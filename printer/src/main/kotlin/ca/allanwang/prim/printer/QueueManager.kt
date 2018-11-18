package ca.allanwang.prim.printer

import ca.allanwang.prim.models.Id
import ca.allanwang.prim.models.PrintedJob
import ca.allanwang.prim.models.ProcessedJob
import ca.allanwang.prim.models.Session
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.concurrent.ConcurrentHashMap

object QueueManager : KoinComponent {

    val printerGroupRepository: PrinterGroupRepository by inject()
    val printerConfigs: PrinterConfiguration by inject()

    private val loadBalancers: MutableMap<Id, LoadBalancer> = ConcurrentHashMap()

    // TODO make thread safe
    private fun loadBalancer(group: Id): LoadBalancer = loadBalancers.getOrElse(group) {
        val loadBalancer = LoadBalancer.fromName(group) ?: printerConfigs.createLoadBalancer(group)
        loadBalancers[group] = loadBalancer
        loadBalancer
    }

    /**
     * Pure function to retrieve a destination printer.
     * Returns null if no suitable printer exists.
     */
    fun getPrinter(group: Id, session: Session, printJob: ProcessedJob): Id? {
        val groupInfo = printerGroupRepository.getPrinters(group) ?: return null
        val printers = groupInfo.second
        return when (printers.size) {
            0 -> null
            1 -> printers.first().id
            else -> {
                val candidates = printerConfigs.getCandidatePrinters(session.role, printers)
                loadBalancer(groupInfo.first.loadBalancer)
                        .select(candidates.map { it.id }.sorted(), printJob)
            }
        }
    }

    /**
     * Consumer to register a new printed job.
     * Used to keep track of destination loads.
     */
    fun registerJob(printJob: PrintedJob) {
        loadBalancer(printJob.printerGroup).register(printJob)
    }

    fun resetLoadBalancer(group: Id) {
        loadBalancers.remove(group)
    }

    fun resetLoadBalancers() {
        loadBalancers.clear()
    }

}
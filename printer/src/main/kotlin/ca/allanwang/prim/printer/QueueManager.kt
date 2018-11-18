package ca.allanwang.prim.printer

import ca.allanwang.prim.models.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

object QueueManager : KoinComponent {

    private val printerGroupRepository: PrinterGroupRepository by inject()
    private val printerConfigs: PrinterConfiguration by inject()

    private val loadBalancers: MutableMap<Id, LoadBalancer> = mutableMapOf()

    @Synchronized
    private fun loadBalancer(group: Id): LoadBalancer = loadBalancers.getOrPut(group) {
        LoadBalancer.fromName(group) ?: printerConfigs.createLoadBalancer(group)
    }

    /**
     * Just checks if there are potentially printers available for the given group and role.
     * Note that no consideration is made in regards to the print job.
     * If the job is processed, use [getPrinter]
     */
    fun hasPrinters(group: Id, role: Role): Boolean {
        val groupInfo = printerGroupRepository.getPrinters(group) ?: return false
        return printerConfigs.getCandidatePrinters(role, groupInfo.second).isNotEmpty()
    }

    /**
     * Pure function to retrieve a destination printer.
     * Returns null if no suitable printer exists.
     */
    fun getPrinter(group: Id, role: Role, printJob: ProcessedJob): Id? {
        val groupInfo = printerGroupRepository.getPrinters(group) ?: return null
        val printers = groupInfo.second
        return when (printers.size) {
            0 -> null
            1 -> printers.first().id
            else -> {
                val candidates = printerConfigs.getCandidatePrinters(role, printers)
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
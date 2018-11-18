package ca.allanwang.prim.printer

import ca.allanwang.prim.models.Flag
import ca.allanwang.prim.models.Id
import ca.allanwang.prim.models.PrintedJob
import ca.allanwang.prim.models.ProcessedJob
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

interface LoadBalancer {
    /**
     * Given a collection of ids and a print request,
     * select one of the ids.
     *
     * Returned id should be one of the candidates.
     *
     * By implementation, the list will have at least 2 candidates;
     * 0 candidates will lead to an automatic rejection, and
     * 1 candidate will lead to an automatic selection.
     *
     * Note that calls here should be idempotent.
     * Only when a job because a request that is passed through [register] should the load balancer change its state.
     * The candidates should also be ordered, in that the same set of candidates should always have the same list.
     */
    fun select(candidates: List<Id>, candidate: ProcessedJob): Id

    /**
     * Makes note of a printed job
     */
    fun register(request: PrintedJob)

    companion object {
        const val DEFAULT = "default"
        const val ROUND_ROBIN = "round_robin"
        const val SHORTEST_WAIT = "shortest_wait"

        /**
         * Get one of the packaged load balancers by key
         */
        fun fromName(name: Flag): LoadBalancer? = when (name) {
            DEFAULT -> RoundRobin()
            ROUND_ROBIN -> RoundRobin()
            SHORTEST_WAIT -> ShortestWait()
            else -> null
        }
    }

}

/**
 * Loops through candidates by index to assign the next job.
 * No consideration is done with regards to the actual job or page count.
 *
 * If the old index cannot be found, it starts back at candidate 0.
 */
class RoundRobin : LoadBalancer {

    private val lastUsed = AtomicReference<Id>("")

    override fun select(candidates: List<Id>, candidate: ProcessedJob): Id {
        val oldIndex = candidates.indexOf(lastUsed.get()) // -1 if not found
        return candidates[(oldIndex + 1) % candidates.size]
    }

    override fun register(request: PrintedJob) {
        lastUsed.set(request.printerGroup)
    }
}

/**
 * Assigns job to the printer deemed to have the lowest wait time
 */
class ShortestWait : LoadBalancer {

    private val endTimes: MutableMap<Id, Long> = ConcurrentHashMap()

    // Gets candidate with the lowest logged end time
    override fun select(candidates: List<Id>, candidate: ProcessedJob): Id =
            candidates.minBy { endTimes.getOrDefault(it, 0L) }!!

    // Gets the old end time, which is either some time in the future or now,
    // and add the duration for the current request
    override fun register(request: PrintedJob) {
//        val oldEnd = Math.max(System.currentTimeMillis(), endTimes.getOrDefault(request.printerGroup, 0L))
//        endTimes[request.destination] = oldEnd + (request.psInfo.pages * Configs.pageToMsFactor)
    }

}

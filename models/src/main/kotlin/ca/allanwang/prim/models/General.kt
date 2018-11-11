package ca.allanwang.prim.models

import java.util.*
import java.util.concurrent.TimeUnit

/**
 * General response for executing requests
 */
data class GeneralResponse(
        val ok: Boolean,
        val id: Id
)

/**
 * General error response
 */
data class ErrorResponse(
        val status: Int,
        val flag: Flag,
        val extras: List<String> = emptyList()
)

data class About(
        val creationTime: Date,
        val startTime: Date,
        val hash: String?,
        val tag: String?,
        val warning: List<String>
) {
    val uptime: String
        get() {
            val up = System.currentTimeMillis() - startTime.time
            val hours = TimeUnit.MILLISECONDS.toHours(up)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(up) - TimeUnit.HOURS.toMinutes(hours)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(up) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(up))
            return String.format("%02d hours, %02d min, %02d sec", hours, minutes, seconds)
        }
}
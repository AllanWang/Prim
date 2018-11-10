package ca.allanwang.ktor.models

import java.util.*

data class PrinterGroup(
        val id: Id,
        val name: String,
        val queueManager: Flag
)

/**
 * Representation of a printer.
 * [name] is the human readable version, and should typically also be unique.
 * [user], [flag], and [message] are from the last associated [PrinterStatus]
 */
data class Printer(
        val id: Id,
        val name: String,
        val groupId: Id,
        val flag: Flag,
        val date: Date,
        val user: User?,
        val message: String?
)

/**
 * Model showing a specific state of a given printer
 * It is expected that the associated printer will have a history of states,
 * and that the most recent one will reflect its current state.
 * If no state is found, the printer is expected to be disabled.
 */
data class PrinterStatus(
        val id: Id,
        val user: User,
        val flag: Flag,
        val date: Date,
        val message: String
) {
    companion object {
        const val FLAG_ENABLED = "enabled"
        const val FLAG_DISABLED = "disabled"
    }
}
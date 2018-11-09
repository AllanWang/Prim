data class PrinterGroup(
        val id: Id,
        val name: String,
        val queueManager: Flag
)

/**
 * Representation of a printer.
 * Name is the human readable version, and should typically also be unique.
 */
data class Printer(
        val id: Id,
        val name: String,
        val groupId: Id,
        val flag: Flag
) {
    companion object {
        const val FLAG_ENABLED = "enabled"
        const val FLAG_DISABLED = "disabled"
        const val FLAG_OFFICE_ONLY = "office_only"
    }
}

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
        val date: DateTime
)

/**
 * Association between a printer status flag and its message.
 * The flag is typically much smaller, and may not be a full sentence.
 * This also allows us to modify the description of an error without affecting previous states.
 */
data class PrinterStatusMessage(
        val flag: Flag,
        val message: String
)
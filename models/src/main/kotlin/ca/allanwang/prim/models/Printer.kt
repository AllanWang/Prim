package ca.allanwang.prim.models

import java.util.*

data class PrinterJson(
        override val id: Id,
        val name: Name,
        val groupId: Id,
        val flag: Flag,
        val statusUser: User? = null,
        val statusDate: Date? = null,
        val statusMessage: String? = null
) : IdModel<Id>, JsonModel<Printer> {
    override fun specific(): Printer = Printer(
            id = id,
            name = name,
            groupId = groupId,
            status = if (statusUser != null
                    && statusMessage != null
                    && statusDate != null)
                PrinterStatus(user = statusUser,
                        message = statusMessage,
                        date = statusDate,
                        flag = flag
                ) else null
    )
}

/**
 * Representation of a printer.
 * [name] is the human readable version, and should typically also be unique.
 */
data class Printer(
        override val id: Id,
        val name: Name,
        val groupId: Id,
        val status: PrinterStatus?
) : IdModel<Id>, SpecificModel<PrinterJson> {

    val flag: Flag get() = status?.flag ?: PrinterStatus.FLAG_DISABLED

    override fun json(): PrinterJson = PrinterJson(
            id = id,
            name = name,
            groupId = groupId,
            flag = flag,
            statusDate = status?.date,
            statusUser = status?.user,
            statusMessage = status?.message
    )
}

/**
 * Model showing a specific state of a given printer
 * It is expected that the associated printer will have a history of states,
 * and that the most recent one will reflect its current state.
 * If no state is found, the printer is expected to be disabled.
 */
data class PrinterStatus(
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

data class PrinterGroup(
        override val id: Id,
        val name: Name,
        val loadBalancer: Flag
) : IdModel<Id>

/**
 * Printer group along with its associated printers.
 * Note that the list should be sorted in ascending order by id.
 * This ensures that each printer group is equal by data.
 */
data class PrinterGroupFull(
        val group: PrinterGroup,
        val printers: List<Printer>
)
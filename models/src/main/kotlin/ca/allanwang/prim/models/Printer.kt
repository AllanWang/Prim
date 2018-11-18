package ca.allanwang.prim.models

import java.util.*

data class PrinterJson(
        val id: Id,
        val name: Name,
        val groupId: Id,
        val flag: Flag,
        val statusUser: User? = null,
        val statusDate: Date? = null,
        val statusMessage: String? = null
) : JsonModel<Printer> {
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
        val id: Id,
        val name: Name,
        val groupId: Id,
        val status: PrinterStatus?
) : SpecificModel<PrinterJson> {

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
        val id: Id,
        val name: Name,
        val loadBalancer: Flag
)
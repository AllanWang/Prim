package ca.allanwang.prim.models

import java.util.*

data class PrinterJson(
        val id: String,
        val name: String,
        val groupId: String,
        val flag: String,
        val statusUser: String? = null,
        val statusDate: Date? = null,
        val statusMessage: String? = null
) : JsonModel<Printer> {
    override fun specific(): Printer = Printer(
            id = Id(id),
            name = name,
            groupId = Id(groupId),
            status = if (statusUser != null
                    && statusMessage != null
                    && statusDate != null)
                PrinterStatus(user = User(statusUser),
                        message = statusMessage,
                        date = statusDate,
                        flag = Flag(flag)
                ) else null
    )
}

/**
 * Representation of a printer.
 * [name] is the human readable version, and should typically also be unique.
 */
data class Printer(
        val id: Id,
        val name: String,
        val groupId: Id,
        val status: PrinterStatus?
): SpecificModel<PrinterJson> {

    val flag: String get() = status?.flag?.flag ?: PrinterStatus.FLAG_DISABLED

    override fun json(): PrinterJson = PrinterJson(
            id = id.value,
            name = name,
            groupId = groupId.value,
            flag = flag,
            statusDate = status?.date,
            statusUser = status?.user?.value,
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
        val name: String,
        val queueManager: Flag
)
package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.ktor.models.*
import ca.allanwang.prim.printer.PrinterRepository
import ca.allanwang.prim.printer.sql.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.Date

object PrinterTable : Table() {
    val id = varchar("id", ID_SIZE).primaryKey().clientDefault(::newId)
    val name = varchar("name", NAME_SIZE).uniqueIndex()
    val group = varchar("group", ID_SIZE)
}

object PrinterStatusTable : Table() {
    val id = varchar("id", ID_SIZE).primaryKey(0).references(PrinterTable.id, ReferenceOption.CASCADE)
    val date = datetime("date").primaryKey(1).clientDefault(DateTime::now)
    val user = varchar("user", USER_SIZE)
    val flag = varchar("flag", FLAG_SIZE)
    val message = varchar("message", MESSAGE_SIZE)
}

internal class PrinterRepositorySql : PrinterRepository {

    private fun ResultRow.toPrinter(): Printer = Printer(
            id = Id(this[PrinterTable.id]),
            name = this[PrinterTable.name],
            groupId = Id(this[PrinterTable.group]),
            flag = Flag(PrinterStatus.FLAG_DISABLED),
            user = null,
            date = Date(),
            message = null
    )

    private fun ResultRow.toPrinterStatus(): PrinterStatus = PrinterStatus(
            id = Id(this[PrinterStatusTable.id]),
            user = User(this[PrinterStatusTable.user]),
            flag = Flag(this[PrinterStatusTable.flag]),
            date = this[PrinterStatusTable.date].toDate(),
            message = this[PrinterStatusTable.message]
    )

    override fun getById(id: Id): Printer? = transaction {

        val printer = PrinterTable.select {
            PrinterTable.id eq id.value
        }.firstOrNull()
                ?.toPrinter()
                ?: return@transaction null

        // If no status found, return current printer model
        val status = PrinterStatusTable.select {
            (PrinterStatusTable.id eq id.value)
        }.orderBy(PrinterStatusTable.date, false)
                .limit(1)
                .firstOrNull()
                ?.toPrinterStatus()
                ?: return@transaction printer

        printer.copy(
                user = status.user,
                flag = status.flag,
                date = status.date,
                message = status.message
        )

    }

    override fun deleteById(id: Id): Unit = transaction {
        PrinterTable.deleteWhere { PrinterTable.id eq id.value }
    }

    override fun getList(limit: Int, offset: Int): List<Printer> = transaction {
        // TODO optimize list retrieval
        PrinterTable.selectAll()
                .mapNotNull { getById(Id(it[PrinterTable.id])) }
    }

    override fun getList(group: PrinterGroup): List<Printer> = transaction {
        // TODO optimize list retrieval
        PrinterTable.select { PrinterTable.group eq group.id.value }
                .mapNotNull { getById(Id(it[PrinterTable.id])) }
    }

    override fun create(name: String, group: PrinterGroup): Printer = transaction {
        val id = PrinterTable.insert {
            it[PrinterTable.name] = name
            it[PrinterTable.group] = group.id.value
        } get PrinterTable.id

        getById(Id(id!!))!!
    }

}
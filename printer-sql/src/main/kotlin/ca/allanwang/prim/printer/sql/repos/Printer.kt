package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.ktor.models.*
import ca.allanwang.prim.models.*
import ca.allanwang.prim.printer.PrinterRepository
import ca.allanwang.prim.printer.sql.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

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

    private fun ResultRow.toPrinter(): Printer = PrinterJson(
            id = this[PrinterTable.id],
            name = this[PrinterTable.name],
            groupId = this[PrinterTable.group],
            flag = tryGet(PrinterStatusTable.flag) ?: PrinterStatus.FLAG_DISABLED,
            statusUser = tryGet(PrinterStatusTable.user),
            statusDate = tryGet(PrinterStatusTable.date)?.toDate(),
            statusMessage = tryGet(PrinterStatusTable.message)
    ).specific()

    override fun getById(id: Id): Printer? = transaction {

        (PrinterTable leftJoin PrinterStatusTable)
                .select { PrinterTable.id eq id.value }
                .firstOrNull()
                ?.toPrinter()

    }

    override fun deleteById(id: Id): Unit = transaction {
        PrinterTable.deleteWhere { PrinterTable.id eq id.value }
    }

    override fun getList(limit: Int, offset: Int): List<Printer> = transaction {
        (PrinterTable leftJoin PrinterStatusTable)
                .selectAll()
                .map { it.toPrinter() }
    }

    override fun getList(group: PrinterGroup): List<Printer> = transaction {
        (PrinterTable leftJoin PrinterStatusTable)
                .select { PrinterTable.group eq group.id.value }
                .map { it.toPrinter() }
    }

    override fun create(name: String, group: PrinterGroup): Printer = transaction {
        val id = PrinterTable.insert {
            it[PrinterTable.name] = name
            it[PrinterTable.group] = group.id.value
        } get PrinterTable.id

        getById(Id(id!!))!!
    }

}
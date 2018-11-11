package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.models.*
import ca.allanwang.prim.printer.PrinterGroupRepository
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

object PrinterGroupTable : Table() {
    val id = varchar("id", ID_SIZE).primaryKey()
    val name = varchar("name", NAME_SIZE).uniqueIndex()
    val queueManager = varchar("queue_manager", FLAG_SIZE)
}

internal object PrinterRepositorySql : PrinterRepository {

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

    override fun create(name: String, group: PrinterGroup): Printer? = transaction {
        val id = PrinterTable.insert {
            it[PrinterTable.name] = name
            it[PrinterTable.group] = group.id.value
        } get PrinterTable.id ?: return@transaction null

        getById(Id(id))
    }

}

internal object PrinterGroupRepositorySql : PrinterGroupRepository {

    private fun ResultRow.toPrinterGroup(): PrinterGroup = PrinterGroup(
            id = Id(this[PrinterGroupTable.id]),
            name = this[PrinterGroupTable.name],
            queueManager = Flag(this[PrinterGroupTable.queueManager]))


    override fun getById(id: Id): PrinterGroup? = transaction {

        PrinterGroupTable.select { PrinterTable.id eq id.value }
                .firstOrNull()
                ?.toPrinterGroup()

    }

    override fun deleteById(id: Id): Unit = transaction {
        PrinterGroupTable.deleteWhere { PrinterGroupTable.id eq id.value }
    }

    override fun getList(limit: Int, offset: Int): List<PrinterGroup> = transaction {
        PrinterGroupTable.selectAll().map { it.toPrinterGroup() }
    }

    override fun create(name: String, queueManager: Flag): PrinterGroup? = transaction {
        val id = PrinterGroupTable.insert {
            it[PrinterGroupTable.name] = name
            it[PrinterGroupTable.queueManager] = queueManager.flag
        } get PrinterGroupTable.id ?: return@transaction null

        getById(Id(id))
    }

    override fun getAllPrinters(): Map<PrinterGroup, List<Printer>> = transaction {
        TODO("not implemented")
    }

    override fun getPrinter(group: Id): Pair<PrinterGroup, List<Printer>>? = transaction {
        val printerGroup = getById(group) ?: return@transaction null
        val printers = PrinterRepositorySql.getList(printerGroup)
        printerGroup to printers
    }

}
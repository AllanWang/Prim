package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.models.*
import ca.allanwang.prim.printer.PrinterGroupRepository
import ca.allanwang.prim.printer.PrinterRepository
import ca.allanwang.prim.printer.sql.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object PrinterTable : IdTable<String>("printer") {
    override val id = varchar("id", ID_SIZE).entityId()
    val name = varchar("name", NAME_SIZE).uniqueIndex()
    val group = varchar("group", ID_SIZE)
}

object PrinterStatusTable : IdTable<String>("printer_status") {
    override val id = varchar("id", ID_SIZE).primaryKey(0).entityId().references(PrinterTable.id, ReferenceOption.CASCADE)
    val date = datetime("date").primaryKey(1).clientDefault(DateTime::now)
    val user = varchar("user", USER_SIZE)
    val flag = varchar("flag", FLAG_SIZE)
    val message = varchar("message", MESSAGE_SIZE)
}

object PrinterGroupTable : IdTable<String>("printer_group") {
    override val id = varchar("id", ID_SIZE).entityId()
    val name = varchar("name", NAME_SIZE).uniqueIndex()
    val loadBalancer = varchar("load_balancer", FLAG_SIZE)
}

internal object PrinterRepositorySql : PrinterRepository,
        SqlRepository<Id, Printer, PrinterTable>(PrinterTable){

    override fun ResultRow.rowToModel(): Printer = PrinterJson(
            id = this[table.id].value,
            name = this[table.name],
            groupId = this[table.group],
            flag = tryGet(PrinterStatusTable.flag) ?: PrinterStatus.FLAG_DISABLED,
            statusUser = tryGet(PrinterStatusTable.user),
            statusDate = tryGet(PrinterStatusTable.date)?.toDate(),
            statusMessage = tryGet(PrinterStatusTable.message)
    ).specific()

    override fun retrieverFields(): FieldSet = (PrinterTable leftJoin PrinterStatusTable)

    override fun getList(group: PrinterGroup): List<Printer> = transaction {
        (PrinterTable leftJoin PrinterStatusTable)
                .select { PrinterTable.group eq group.id }
                .map { it.rowToModel() }
    }

    override fun create(id: Id, name: Name, group: PrinterGroup): Printer? = transactionInsert {
        it[table.id] = EntityID(id, table)
        it[table.name] = name
        it[table.group] = group.id
    }

    override fun count(): Int = transaction {
        PrinterTable.selectAll().count()
    }
}

internal object PrinterGroupRepositorySql : PrinterGroupRepository,
        SqlRepository<Id, PrinterGroup, PrinterGroupTable>(PrinterGroupTable) {

    override fun ResultRow.rowToModel(): PrinterGroup = PrinterGroup(
            id = this[table.id].value,
            name = this[table.name],
            queueManager = this[table.loadBalancer])

    override fun create(id: Id, name: Name, loadBalancer: Flag): PrinterGroup? = transactionInsert {
        it[table.id] = EntityID(id, table)
        it[table.name] = name
        it[table.loadBalancer] = loadBalancer
    }

    override fun updateLoadBalancer(id: Id, loadBalancer: Flag): PrinterGroup? = transactionSingleUpdate(id) {
        it[table.loadBalancer] = loadBalancer
    }

    override fun getAllPrinters(): Map<PrinterGroup, List<Printer>> = transaction {
        val groups = getList()
        val printers = PrinterRepositorySql.getList().groupBy { it.groupId }
        groups.map { it to (printers[it.id] ?: emptyList()) }.toMap()
    }

    override fun getPrinter(group: Id): Pair<PrinterGroup, List<Printer>>? = transaction {
        val printerGroup = getById(group) ?: return@transaction null
        val printers = PrinterRepositorySql.getList(printerGroup)
        printerGroup to printers
    }

}
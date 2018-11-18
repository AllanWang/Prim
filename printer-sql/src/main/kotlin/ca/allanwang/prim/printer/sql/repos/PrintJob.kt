package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.models.*
import ca.allanwang.prim.printer.Order
import ca.allanwang.prim.printer.PrintJobRepository
import ca.allanwang.prim.printer.newId
import ca.allanwang.prim.printer.sql.FLAG_SIZE
import ca.allanwang.prim.printer.sql.ID_SIZE
import ca.allanwang.prim.printer.sql.SqlRepository
import ca.allanwang.prim.printer.sql.USER_SIZE
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object PrintJobTable : IdTable<String>("print_job") {
    override val id = varchar("id", ID_SIZE).clientDefault(::newId).entityId()
    val flag = varchar("flag", FLAG_SIZE).default(PrintJob.CREATED)
    val user = varchar("user", USER_SIZE).uniqueIndex()
    val jobName = varchar("job_name", ID_SIZE)
    val printerGroup = varchar("printer_group", ID_SIZE)
    val filePath = varchar("file_path", ID_SIZE).nullable()
    val totalPageCount = integer("total_page_count").default(0)
    val colorPageCount = integer("color_page_count").default(0)
    val createdAt = datetime("created_at").clientDefault(DateTime::now)
    val processedAt = datetime("processed_at").nullable()
    val finishedAt = datetime("finished_at").nullable()
    val errorFlag = varchar("error_flag", FLAG_SIZE).nullable()
    val printer = varchar("printer", ID_SIZE).nullable()
}

object PrintJobRefundTable : IdTable<String>("print_job_refund") {
    override val id = varchar("id", ID_SIZE).primaryKey(0).entityId().references(PrintJobTable.id)
    val date = datetime("date").clientDefault(DateTime::now).primaryKey(1)
    val refunder = varchar("refunder", USER_SIZE)
    val refund = bool("refund")
}

internal object PrintJobRepositorySql : PrintJobRepository,
        SqlRepository<Id, PrintJob, PrintJobTable>(PrintJobTable) {

    override fun ResultRow.rowToModel(): PrintJob = PrintJobJson(
            id = this[table.id].value,
            flag = this[table.flag],
            user = this[table.user],
            jobName = this[table.jobName],
            printerGroup = this[table.printerGroup],
            filePath = this[table.filePath],
            totalPageCount = this[table.totalPageCount],
            colorPageCount = this[table.colorPageCount],
            createdAt = this[table.createdAt].toDate(),
            processedAt = this[table.processedAt]?.toDate(),
            finishedAt = this[table.finishedAt]?.toDate(),
            errorFlag = this[table.errorFlag],
            printer = this[table.printer],
            refundDate = tryGet(PrintJobRefundTable.date)?.toDate(),
            refunded = tryGet(PrintJobRefundTable.refund) ?: false,
            refunder = tryGet(PrintJobRefundTable.refunder)
    ).specific()

    // TODO allow for multiple refunds and get the latest one only
    override fun retrieverFields(): FieldSet = table leftJoin PrintJobRefundTable

    override fun create(user: User, jobName: String, printerGroup: Id): CreatedJob? = transactionInsert(newId()) {
        it[this.flag] = PrintJob.CREATED
        it[this.jobName] = jobName
        it[this.user] = user
        it[this.printerGroup] = printerGroup
    } as CreatedJob?

    override fun updateProcessed(id: Id,
                                 filePath: String,
                                 totalPageCount: Int,
                                 colorPageCount: Int): ProcessedJob? = transactionUpdate(id, {
        (table.id eq id) and ((table.flag eq PrintJob.CREATED) or (table.flag eq PrintJob.PROCESSED))
    }) {
        it[this.flag] = PrintJob.PROCESSED
        it[this.processedAt] = DateTime.now()
        it[this.filePath] = filePath
        it[this.totalPageCount] = totalPageCount
        it[this.colorPageCount] = colorPageCount
    } as ProcessedJob?

    override fun updatePrinted(id: Id, printer: Id): PrintedJob? = transactionUpdate(id, {
        (table.id eq id) and ((table.flag eq PrintJob.PROCESSED) or (table.flag eq PrintJob.PRINTED))
    }) {
        it[this.flag] = PrintJob.PRINTED
        it[this.finishedAt] = DateTime.now()
        it[this.printer] = printer
    } as PrintedJob?

    override fun updateFailed(id: Id, error: Flag): FailedJob? = transactionUpdate(id, {
        (table.id eq id) and (table.flag neq PrintJob.PRINTED)
    }) {
        it[this.flag] = PrintJob.FAILED
        it[this.finishedAt] = DateTime.now()
        it[this.errorFlag] = error
    } as FailedJob?

    override fun getListByPrinterGroup(printerGroup: Id, limit: Int, offset: Int, order: Order): List<PrintJob> = transaction {
        table.select { table.printerGroup eq printerGroup }.limit(limit, offset).map { it.rowToModel() }
    }

    override fun updateRefundStatus(id: Id, refunder: User, refund: Boolean): PrintedJob? = transaction {
        if (table.select { (table.id eq id) and (table.flag eq PrintJob.PRINTED) }.count() == 0) return@transaction null
        getById(id)
    } as PrintedJob?
}
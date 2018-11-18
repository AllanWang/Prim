package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.models.PrintJob
import ca.allanwang.prim.printer.repos.PrintJobTestBase
import ca.allanwang.prim.printer.repos.PrintJobTestRepository
import ca.allanwang.prim.printer.sql.SqlExtension
import ca.allanwang.prim.printer.sql.sqlRepositoryModule
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.dsl.module.module
import kotlin.test.Test

class PrintJobExtension : SqlExtension(
        tables = listOf(PrintJobTable, PrintJobRefundTable),
        modules = listOf(sqlRepositoryModule, sqlTestPrintJobModule))

@ExtendWith(PrintJobExtension::class)
class PrintJobTest : PrintJobTestBase() {


    @Test
    fun blank() {

    }

    /*
     * TODO
     *
     * - default refund status should be false
     * - adding one refund status should work
     * - various update attempts where flags do not match
     */

}

val sqlTestPrintJobModule = module {
    single<PrintJobTestRepository> { PrintJobTestRepositorySql }
}

internal object PrintJobTestRepositorySql : PrintJobTestRepository {

    private val table = PrintJobTable

    override fun save(job: PrintJob): Unit = transaction {
        val json = job.json()
        table.replace {
            it[id] = EntityID(json.id, table)
            it[flag] = json.flag
            it[user] = json.user
            it[filePath] = json.filePath
            it[totalPageCount] = json.totalPageCount
            it[colorPageCount] = json.colorPageCount
            it[createdAt] = DateTime(json.createdAt)
            it[processedAt] = json.processedAt?.let(::DateTime)
            it[finishedAt] = json.finishedAt?.let(::DateTime)
            it[errorFlag] = json.errorFlag
            it[printer] = json.printer
            it[printerGroup] = json.printerGroup
        }
    }
}
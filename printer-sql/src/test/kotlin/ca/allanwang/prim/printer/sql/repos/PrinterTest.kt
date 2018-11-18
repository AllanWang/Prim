package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.printer.repos.PrinterTestBase
import ca.allanwang.prim.printer.sql.SqlExtension
import ca.allanwang.prim.printer.sql.sqlRepositoryModule
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

class PrinterExtension : SqlExtension(
        tables = listOf(PrinterTable, PrinterStatusTable, PrinterGroupTable),
        modules = listOf(sqlRepositoryModule))

@ExtendWith(PrinterExtension::class)
class PrinterTest : PrinterTestBase() {


    @Test
    fun blank() {

    }

    /*
     * TODO
     *
     * - default status should be disabled
     * - adding one status should work
     * - updating a status many times should always result in the last status being used
     * - adding a status to an invalid printer id should result in no changes
     */

}
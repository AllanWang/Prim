package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.printer.repos.PrinterTestBase
import ca.allanwang.prim.printer.sql.SqlExtension
import ca.allanwang.prim.printer.sql.sqlRepositoryModule
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrinterExtension : SqlExtension(
        tables = listOf(PrinterTable, PrinterStatusTable, PrinterGroupTable),
        modules = listOf(sqlRepositoryModule))

@ExtendWith(PrinterExtension::class)
class PrinterTest : PrinterTestBase() {


    @Test
    fun `basic group creation 2`() {
        val group = createPrinterGroup(0)!!
        val groupFromDb = printerGroupRepository.getPrinter(group.id)!!
        assertTrue(groupFromDb.second.isEmpty(),
                "New printer group should have empty printer list")
        assertEquals(group, groupFromDb.first,
                "Printer retrieved from db does not match original")
        assertNull(printerGroupRepository.getPrinter(
                "invalidGroup"), "Printer with different group should return null")
    }

}
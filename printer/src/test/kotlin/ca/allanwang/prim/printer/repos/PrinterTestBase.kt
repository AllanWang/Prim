package ca.allanwang.prim.printer.repos

import ca.allanwang.prim.models.Id
import ca.allanwang.prim.models.Name
import ca.allanwang.prim.models.Printer
import ca.allanwang.prim.models.PrinterGroup
import ca.allanwang.prim.printer.PrinterGroupRepository
import ca.allanwang.prim.printer.PrinterRepository
import org.koin.standalone.inject
import kotlin.test.*

/**
 * Base test suite for session related components
 * Required koins:
 * - [PrinterRepository]
 * - [PrinterGroupRepository]
 */
abstract class PrinterTestBase : TestBase() {

    val printerRepository: PrinterRepository by inject()
    val printerGroupRepository: PrinterGroupRepository by inject()

    fun createPrinter(key: Int = 0,
                      group: PrinterGroup,
                      id: Id = "testId$key",
                      name: Name = "testName$key") =
            printerRepository.create(id = id, name = name, group = group)

    fun createPrinterGroup(key: Int = 0,
                           id: Id = "testGroupId$key",
                           name: Name = "testGroupName$key",
                           queueManager: String = "testQueueManager$key") =
            printerGroupRepository.create(id = id, name = name, loadBalancer = queueManager)

    fun assertPrinterGroupsEqual(expected: Pair<PrinterGroup, List<Printer>>?, actual: Pair<PrinterGroup, List<Printer>>?, message: String? = null) {
        when {
            expected == null && actual == null -> Unit
            expected != null && actual != null -> {
                assertEquals(expected.first, actual.first, message ?: "Printer group name mismatch")
                assertEquals(expected.second.sortedBy { it.id }, actual.second.sortedBy { it.id }, message
                        ?: "Printer list mismatch")
            }
            else -> fail(message ?: "Printer group mismatch")
        }
    }

    @Test
    fun `basic group creation`() {
        val group = createPrinterGroup(0)!!
        val groupFromDb = printerGroupRepository.getPrinter(group.id)!!
        assertTrue(groupFromDb.second.isEmpty(),
                "New printer group should have empty printer list")
        assertEquals(group, groupFromDb.first,
                "Printer retrieved from db does not match original")
        assertNull(printerGroupRepository.getPrinter(
                "invalidGroup"), "Printer with different group should return null")
    }

    @Test
    fun `adding two groups with the same id returns null`() {
        assertNotNull(createPrinterGroup(0),
                "New group not added properly")
        assertNull(createPrinterGroup(0),
                "Second group with same id should not have been added properly")
    }

    @Test
    fun `basic group and printer tests`() {
        val group0 = createPrinterGroup(0)!!
        val group1 = createPrinterGroup(1)!!
        val printer00 = createPrinter(0, group0)!!
        val printer01 = createPrinter(1, group0)!!
        val printer12 = createPrinter(2, group1)!!

        val groupDb0 = printerGroupRepository.getPrinter(group0.id)
        val groupDb1 = printerGroupRepository.getPrinter(group1.id)
        assertPrinterGroupsEqual(group0 to listOf(printer00, printer01), groupDb0)
        assertPrinterGroupsEqual(group1 to listOf(printer12), groupDb1)

        val allGroupsDb = printerGroupRepository.getAllPrinters()
        assertPrinterGroupsEqual(groupDb0, group0 to allGroupsDb.getValue(group0),
                "All printers did not contain group0 data")
        assertPrinterGroupsEqual(groupDb1, group1 to allGroupsDb.getValue(group1),
                "All printers did not contain group1 data")
    }

    @Test
    fun `update group load balancer`() {
        val group = createPrinterGroup(0)!!
        assertTrue(group.loadBalancer.isNotEmpty(),
                "Default load balancer should not be an empty key")
        val updatedGroup = printerGroupRepository.updateLoadBalancer(group.id, group.loadBalancer.repeat(2))!!
        assertNotEquals(group, updatedGroup,
                "Updating load balancer did not change anything.")
        assertEquals(group, updatedGroup.copy(loadBalancer = group.loadBalancer),
                "Fields beyond load balancer changed when updating load balancer")
    }

}
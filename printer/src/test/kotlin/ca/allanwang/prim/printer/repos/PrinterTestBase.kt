package ca.allanwang.prim.printer.repos

import ca.allanwang.prim.models.*
import ca.allanwang.prim.printer.PrinterGroupRepository
import ca.allanwang.prim.printer.PrinterRepository
import org.koin.standalone.inject
import kotlin.test.*

/**
 * Base test suite for [Printer] and [PrinterGroup] related components
 * Required koins:
 * - [PrinterRepository]
 * - [PrinterGroupRepository]
 */
abstract class PrinterTestBase : TestBase() {

    val printerRepository: PrinterRepository by inject()
    val printerGroupRepository: PrinterGroupRepository by inject()

    fun createPrinter(key: Int = 0,
                      id: Id = "testId$key",
                      name: Name = "testName$key",
                      groupId: Id = "group$key") =
            printerRepository.create(id = id, name = name, groupId = groupId)

    fun createPrinterGroup(key: Int = 0,
                           id: Id = "testGroupId$key",
                           name: Name = "testGroupName$key",
                           queueManager: String = "testQueueManager$key") =
            printerGroupRepository.create(id = id, name = name, loadBalancer = queueManager)

    @Test
    fun `basic group creation`() {
        val group = createPrinterGroup(0)!!
        val groupFromDb = printerGroupRepository.getPrinters(group.id)!!
        assertTrue(groupFromDb.printers.isEmpty(),
                "New printer group should have empty printer list")
        assertEquals(group, groupFromDb.group,
                "Printer retrieved from db does not match original")
        assertNull(printerGroupRepository.getPrinters(
                "invalidGroup"), "Printer with different group should return null")
    }

    @Test
    fun `adding the same group a second time should return null`() {
        assertNotNull(createPrinterGroup(0),
                "New group not added properly")
        assertNull(createPrinterGroup(0),
                "Second group with same id should not have been added properly")
    }

    @Test
    fun `basic group and printer tests`() {
        val group0 = createPrinterGroup(0)!!
        val group1 = createPrinterGroup(1)!!
        val printer00 = createPrinter(0, groupId = group0.id)!!
        val printer01 = createPrinter(1, groupId = group0.id)!!
        val printer12 = createPrinter(2, groupId = group1.id)!!

        val groupDb0 = printerGroupRepository.getPrinters(group0.id)
        val groupDb1 = printerGroupRepository.getPrinters(group1.id)
        assertEquals(PrinterGroupFull(group0, listOf(printer00, printer01)), groupDb0)
        assertEquals(PrinterGroupFull(group1, listOf(printer12)), groupDb1)

        val allGroupsDb = printerGroupRepository.getAllPrinters()
        assertEquals(groupDb0, PrinterGroupFull(group0, allGroupsDb.getValue(group0)),
                "All printers did not contain group0 data")
        assertEquals(groupDb1, PrinterGroupFull(group1, allGroupsDb.getValue(group1)),
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

    @Test
    fun `default list sort`() {
        assertListSortedById(printerRepository) {
            createPrinter(it)!!
        }
    }

}
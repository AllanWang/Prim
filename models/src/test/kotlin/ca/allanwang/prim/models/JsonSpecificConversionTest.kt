package ca.allanwang.prim.models

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonSpecificConversionTest {

    private inline fun <reified T : SpecificModel<*>> T.verify() {
        val json = json()
        assertTrue(json is JsonModel<*>, "Resulting json is not a json model for ${T::class.simpleName}")
        assertEquals(this, json.specific(), "Converted model for ${T::class.simpleName} does not match original")
    }

    @Test
    fun printers() {
        val printer = Printer(
                id = "testId",
                name = "testName",
                groupId = "testGroupId",
                status = null)

        printer.verify()

        printer.copy(status = PrinterStatus(
                user = "testUser",
                date = Date(1),
                message = "testMessage",
                flag = "testFlag")).verify()
    }

    @Test
    fun printJobs() {
        CreatedJob(
                id = "testId",
                user = "testUser",
                createdAt = Date(1),
                printerGroup = "group").verify()

        ProcessedJob(
                id = "testId",
                user = "testUser",
                createdAt = Date(1),
                totalPageCount = 1,
                colorPageCount = 2,
                filePath = "filePath",
                processedAt = Date(5),
                printerGroup = "group").verify()

        val printed = PrintedJob(
                id = "testId",
                user = "testUser",
                createdAt = Date(1),
                totalPageCount = 1,
                colorPageCount = 2,
                filePath = "filePath",
                processedAt = Date(5),
                refund = null,
                finishedAt = Date(7),
                printerGroup = "group",
                printer = "printer")

        printed.verify()

        printed.copy(refund = PrintRefund(
                refunder = "testRefundUser",
                date = Date(8))).verify()

        FailedJob(
                id = "testId",
                user = "testUser",
                finishedAt = Date(1),
                errorFlag = "error",
                printer = "printer").verify()
    }


}
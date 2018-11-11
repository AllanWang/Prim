package ca.allanwang.prim.models

import ca.allanwang.ktor.models.*
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
                id = Id("testId"),
                name = "testName",
                groupId = Id("testGroupId"),
                status = null)

        printer.verify()

        printer.copy(status = PrinterStatus(
                user = User("testUser"),
                date = Date(1),
                message = "testMessage",
                flag = Flag("testFlag"))).verify()
    }

    @Test
    fun printJobs() {
        CreatedJob(
                id = Id("testId"),
                user = User("testUser"),
                createdAt = Date(1)).verify()

        ProcessedJob(
                id = Id("testId"),
                user = User("testUser"),
                createdAt = Date(1),
                pageCount = 1,
                colorPageCount = 2,
                filePath = "filePath",
                processedAt = Date(5)).verify()

        val printed = PrintedJob(
                id = Id("testId"),
                user = User("testUser"),
                createdAt = Date(1),
                pageCount = 1,
                colorPageCount = 2,
                filePath = "filePath",
                processedAt = Date(5),
                refund = null,
                finishedAt = Date(7),
                printerGroup = "group")

        printed.verify()

        printed.copy(refund = PrintRefund(
                refunder = User("testRefundUser"),
                date = Date(8))).verify()

        FailedJob(
                id = Id("testId"),
                user = User("testUser"),
                finishedAt = Date(1),
                errorFlag = "error").verify()
    }


}
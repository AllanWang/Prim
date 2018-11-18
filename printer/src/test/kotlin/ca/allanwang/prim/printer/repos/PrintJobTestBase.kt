package ca.allanwang.prim.printer.repos

import ca.allanwang.prim.models.*
import ca.allanwang.prim.printer.PrintJobRepository
import org.koin.standalone.inject
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * Base test suite for [PrintJob] related components
 * Required koins:
 * - [PrintJobRepository]
 * - [PrintJobTestRepository]
 */
abstract class PrintJobTestBase : TestBase() {

    val printJobRepository: PrintJobRepository by inject()
    val printJobTestRepository: PrintJobTestRepository by inject()

    fun createPrintJob(key: Int = 0,
                       user: User = "testUser$key",
                       jobName: String = "job$key",
                       printGroup: Id = "testGroup$key") =
            printJobRepository.create(user = user, jobName = jobName, printerGroup = printGroup)

    @Test
    fun `basic creation`() {
        val job = createPrintJob(0)!!.json()
        assertEquals(PrintJob.CREATED, job.flag)
    }

    /**
     * A series of tests to make sure that [PrintJobTestRepository.save]
     * works as expected.
     */
    @Test
    fun `test module check`() {
        val created = createPrintJob(0)!!
        val id = created.id

        fun <T : PrintJob> T.assertSave(): T {
            val tag = this::class.simpleName
            save()
            assertEquals(this, printJobRepository.getById(id),
                    "$tag test save failed")
            return this
        }

        created.toProcessed().assertSave()
                .toPrinted().assertSave()
                .fail().assertSave()
    }

    /**
     * [PrintJobRepository.updateProcessed] should affect jobs that are in the
     * [PrintJob.CREATED] or [PrintJob.PROCESSED] stages, but not others.
     */
    @Test
    fun `update processed`() {
        val job = createPrintJob(0)!!
        val expectedProcessedJob = job.toProcessed()
        val jobProcessed = printJobRepository.updateProcessed(expectedProcessedJob.id,
                filePath = expectedProcessedJob.filePath,
                totalPageCount = expectedProcessedJob.totalPageCount,
                colorPageCount = expectedProcessedJob.colorPageCount)!!
        assertEquals(expectedProcessedJob.copy(processedAt = jobProcessed.processedAt), jobProcessed,
                "Updating created job to processed did not work")

        val jobProcessed2 = printJobRepository.updateProcessed(expectedProcessedJob.id,
                filePath = expectedProcessedJob.filePath,
                totalPageCount = expectedProcessedJob.totalPageCount + 5,
                colorPageCount = expectedProcessedJob.colorPageCount)!!

        assertNotEquals(jobProcessed.totalPageCount, jobProcessed2.totalPageCount,
                "Page count did not change when updating processed job")

        assertEquals(jobProcessed.copy(processedAt = jobProcessed2.processedAt, totalPageCount = jobProcessed2.totalPageCount), jobProcessed2,
                "Other components changed when updating total page count for job")

        assertUnchanged(jobProcessed2.fail(), jobProcessed2.toPrinted()) {
            updateProcessed(it, filePath = "error", totalPageCount = 10, colorPageCount = 2)
        }
    }

    /**
     * [PrintJobRepository.updatePrinted] should affect jobs that are in the
     * [PrintJob.PROCESSED] or [PrintJob.PRINTED] stages, but not others.
     */
    @Test
    fun `update printed`() {
        val job = createPrintJob(0)!!.toProcessed()
        job.save()

        val expectedPrintedJob = job.toPrinted()

        val jobPrinted = printJobRepository.updatePrinted(expectedPrintedJob.id,
                printer = expectedPrintedJob.printer)!!

        assertEquals(expectedPrintedJob.copy(finishedAt = jobPrinted.finishedAt), jobPrinted,
                "Updating processed job to printed did not work")

        val jobPrinted2 = printJobRepository.updatePrinted(expectedPrintedJob.id,
                printer = "${expectedPrintedJob.printer}_2")!!

        assertNotEquals(jobPrinted.printer, jobPrinted2.printer,
                "Printer did not change when updating printed job")

        assertEquals(jobPrinted.copy(finishedAt = jobPrinted2.finishedAt, printer = jobPrinted2.printer), jobPrinted2,
                "Other components changed when updating printer for job")

        assertUnchanged(jobPrinted2.fail(), jobPrinted2.json().copy(flag = PrintJob.CREATED).specific()) {
            updatePrinted(it, printer = "error")
        }
    }

    /*
     * -------------------------------------------------------------------
     * Additional helpers
     * -------------------------------------------------------------------
     */

    fun PrintJob.save() = printJobTestRepository.save(this)

    fun PrintJob.fail(): FailedJob = json().copy(
            flag = PrintJob.FAILED,
            errorFlag = "test_flag",
            finishedAt = Date()).specific() as FailedJob

    fun CreatedJob.toProcessed(): ProcessedJob = ProcessedJob(
            id = id,
            user = user,
            createdAt = createdAt,
            jobName = jobName,
            printerGroup = printerGroup,
            colorPageCount = 5,
            totalPageCount = 15,
            filePath = "test_path",
            processedAt = Date()
    )

    fun ProcessedJob.toPrinted(): PrintedJob = PrintedJob(
            id = id,
            user = user,
            createdAt = createdAt,
            jobName = jobName,
            printerGroup = printerGroup,
            colorPageCount = colorPageCount,
            totalPageCount = totalPageCount,
            filePath = filePath,
            processedAt = processedAt,
            finishedAt = Date(),
            printer = "test_printer",
            refund = null
    )

    /**
     * Helper to ensure that job updates do not apply to incorrect stages.
     * For each supplied job, we will save it, attempt to update it, and make sure the update both returns null
     * and does not modify the db.
     */
    inline fun <reified To : PrintJob> assertUnchanged(
            vararg jobs: PrintJob, updater: PrintJobRepository.(Id) -> To?) {
        val to = To::class.simpleName
        jobs.forEach { job ->
            val from = job::class.simpleName
            job.save()
            assertNull(printJobRepository.updater(job.id), "$to updater should not apply to $from")
            assertEquals(job, printJobRepository.getById(job.id), "invalid update from $from to $to ended up modifying data")
        }
    }

}

interface PrintJobTestRepository {

    /**
     * Test method to insert or update a print job.
     * Do not add any safety checks.
     */
    fun save(job: PrintJob)

}
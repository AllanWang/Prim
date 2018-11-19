package ca.allanwang.prim.models

import java.util.*

data class PrintJobJson(
        val flag: Flag,
        val id: Id,
        val user: User,
        val jobName: String,
        val printerGroup: Id,
        val printer: Id? = null,
        val createdAt: Date? = null,
        val filePath: String? = null,
        val processedAt: Date? = null,
        val totalPageCount: Int = 0,
        val colorPageCount: Int = 0,
        val finishedAt: Date? = null,
        val errorFlag: Flag? = null,
        val refunded: Boolean = false,
        val refunder: User? = null,
        val refundDate: Date? = null
) : JsonModel<PrintJob> {
    /**
     * Converts json model to one of the sealed [PrintJob] classes.
     * Opposite of [PrintJob.json]
     */
    override fun specific(): PrintJob = when (flag) {
        PrintJob.CREATED -> CreatedJob(
                id = id,
                user = user,
                jobName = jobName,
                printerGroup = printerGroup,
                createdAt = createdAt!!
        )
        PrintJob.PROCESSED -> ProcessedJob(
                id = id,
                user = user,
                jobName = jobName,
                printerGroup = printerGroup,
                createdAt = createdAt!!,
                filePath = filePath!!,
                processedAt = processedAt!!,
                totalPageCount = totalPageCount,
                colorPageCount = colorPageCount
        )
        PrintJob.PRINTED -> PrintedJob(
                id = id,
                user = user,
                jobName = jobName,
                printerGroup = printerGroup,
                printer = printer!!,
                createdAt = createdAt!!,
                filePath = filePath!!,
                processedAt = processedAt!!,
                totalPageCount = totalPageCount,
                colorPageCount = colorPageCount,
                finishedAt = finishedAt!!,
                refund = if (refunder != null
                        && refundDate != null)
                    PrintRefund(
                            refunder = refunder,
                            date = refundDate
                    ) else null
        )
        PrintJob.FAILED -> FailedJob(
                id = id,
                user = user,
                jobName = jobName,
                printerGroup = printerGroup,
                printer = printer,
                errorFlag = errorFlag!!,
                finishedAt = finishedAt!!
        )
        else -> throw IllegalArgumentException("Illegal print flag $flag")
    }
}

/**
 * Sealed print job. Note that there are a few stages, each with their own parameters:
 * [CreatedJob] - job has been created by user
 * [ProcessedJob] - file has been attached and processed
 * [PrintedJob] - file has been printed; note that no guarantees exist on the existence of the file anymore
 * [FailedJob] - file failed to print
 *
 * In a given lifecycle, a print job is expected to eventually end up as printed or failed
 */
sealed class PrintJob : SpecificModel<PrintJobJson> {
    abstract val id: Id
    abstract val user: User
    abstract val jobName: String
    abstract val printerGroup: Id

    companion object {
        const val CREATED = "created"
        const val PROCESSED = "processed"
        const val PRINTED = "printed"
        const val FAILED = "failed"
    }
}

data class CreatedJob(
        override val id: Id,
        override val user: User,
        override val jobName: String,
        override val printerGroup: Id,
        val createdAt: Date
) : PrintJob() {
    override fun json() = PrintJobJson(
            flag = CREATED,
            id = id,
            user = user,
            jobName = jobName,
            printerGroup = printerGroup,
            createdAt = createdAt)
}

// TODO add ReceivedJob; getting filePath is separate from getting actual file info

data class ProcessedJob(
        override val id: Id,
        override val user: User,
        override val jobName: String,
        override val printerGroup: Id,
        val createdAt: Date,
        val filePath: String,
        val processedAt: Date,
        val totalPageCount: Int,
        val colorPageCount: Int
) : PrintJob() {
    override fun json() = PrintJobJson(
            flag = PROCESSED,
            id = id,
            user = user,
            jobName = jobName,
            printerGroup = printerGroup,
            createdAt = createdAt,
            filePath = filePath,
            processedAt = processedAt,
            totalPageCount = totalPageCount,
            colorPageCount = colorPageCount)
}

data class PrintedJob(
        override val id: Id,
        override val user: User,
        override val jobName: String,
        override val printerGroup: Id,
        val createdAt: Date,
        val filePath: String?,
        val processedAt: Date,
        val totalPageCount: Int,
        val colorPageCount: Int,
        val finishedAt: Date,
        val printer: Id,
        val refund: PrintRefund?
) : PrintJob() {

    val isRefunded: Boolean get() = refund != null

    override fun json() = PrintJobJson(
            flag = PRINTED,
            id = id,
            user = user,
            jobName = jobName,
            printerGroup = printerGroup,
            printer = printer,
            createdAt = createdAt,
            filePath = filePath,
            processedAt = processedAt,
            totalPageCount = totalPageCount,
            colorPageCount = colorPageCount,
            finishedAt = finishedAt,
            refunded = isRefunded,
            refunder = refund?.refunder,
            refundDate = refund?.date)
}

data class FailedJob(
        override val id: Id,
        override val user: User,
        override val jobName: String,
        override val printerGroup: Id,
        val printer: String?,
        val errorFlag: Flag,
        val finishedAt: Date
) : PrintJob() {
    override fun json() = PrintJobJson(
            flag = FAILED,
            id = id,
            user = user,
            jobName = jobName,
            printerGroup = printerGroup,
            printer = printer,
            errorFlag = errorFlag,
            finishedAt = finishedAt)

    companion object {
        const val GENERIC = "generic"
        const val NO_PRINTERS = "no_printers"
        const val IO_FAILURE = "io_failure"
        const val DELETED_JOB = "deleted_job"
    }
}

/**
 * Model for print job refunds
 * A print job will have at most one associated refund.
 * If none exist, it is not refunded.
 */
data class PrintRefund(
        val refunder: User,
        val date: Date
)

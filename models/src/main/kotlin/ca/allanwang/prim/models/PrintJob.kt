package ca.allanwang.prim.models

import java.util.*

data class PrintJobJson(
        val flag: String,
        val id: String,
        val user: String,
        val printerGroup: Id? = null,
        val printer: Id? = null,
        val createdAt: Date? = null,
        val filePath: String? = null,
        val processedAt: Date? = null,
        val totalPageCount: Int = 0,
        val colorPageCount: Int = 0,
        val finishedAt: Date? = null,
        val errorFlag: String? = null,
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
                printerGroup = printerGroup!!,
                createdAt = createdAt!!
        )
        PrintJob.PROCESSED -> ProcessedJob(
                id = id,
                user = user,
                printerGroup = printerGroup!!,
                createdAt = createdAt!!,
                filePath = filePath!!,
                processedAt = processedAt!!,
                totalPageCount = totalPageCount,
                colorPageCount = colorPageCount
        )
        PrintJob.PRINTED -> PrintedJob(
                id = id,
                user = user,
                printerGroup = printerGroup!!,
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
        val printerGroup: Id,
        val createdAt: Date
) : PrintJob() {
    override fun json() = PrintJobJson(
            CREATED,
            id,
            user,
            printerGroup = printerGroup,
            createdAt = createdAt)
}

data class ProcessedJob(
        override val id: Id,
        override val user: User,
        val printerGroup: Id,
        val createdAt: Date,
        val filePath: String,
        val processedAt: Date,
        val totalPageCount: Int,
        val colorPageCount: Int
) : PrintJob() {
    override fun json() = PrintJobJson(
            PROCESSED,
            id,
            user,
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
        val printerGroup: Id,
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
            PRINTED,
            id,
            user,
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
        val printer: String?,
        val errorFlag: String,
        val finishedAt: Date
) : PrintJob() {
    override fun json() = PrintJobJson(
            FAILED,
            id,
            user,
            printer = printer,
            errorFlag = errorFlag,
            finishedAt = finishedAt)
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

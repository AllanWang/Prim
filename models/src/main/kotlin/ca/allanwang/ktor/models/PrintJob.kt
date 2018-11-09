package ca.allanwang.ktor.models

import java.util.*

data class PrintJobJson(
        val flag: String,
        val id: Id,
        val user: User,
        val createdAt: Date? = null,
        val filePath: String? = null,
        val processedAt: Date? = null,
        val pageCount: Int = 0,
        val colorPageCount: Int = 0,
        val printerGroup: String? = null,
        val finishedAt: Date? = null,
        val errorFlag: String? = null
) {
    /**
     * Converts json model to one of the sealed [PrintJob] classes.
     * Opposite of [PrintJob.json]
     */
    fun specific(): PrintJob = when (flag) {
        PrintJob.CREATED -> CreatedJob(
                id = id,
                user = user,
                createdAt = createdAt!!
        )
        PrintJob.PROCESSED -> ProcessedJob(
                id = id,
                user = user,
                createdAt = createdAt!!,
                filePath = filePath!!,
                processedAt = processedAt!!,
                pageCount = pageCount,
                colorPageCount = colorPageCount
        )
        PrintJob.PRINTED -> PrintedJob(
                id = id,
                user = user,
                createdAt = createdAt!!,
                filePath = filePath!!,
                processedAt = processedAt!!,
                pageCount = pageCount,
                colorPageCount = colorPageCount,
                printerGroup = printerGroup!!,
                finishedAt = finishedAt!!
        )
        PrintJob.FAILED -> FailedJob(
                id = id,
                user = user,
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
sealed class PrintJob {
    abstract val id: Id
    abstract val user: User

    /**
     * Converts sealed printjob to a json model.
     * Opposite of [PrintJobJson.specific]
     */
    abstract fun json(): PrintJobJson

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
        val createdAt: Date
) : PrintJob() {
    override fun json() = PrintJobJson(
            CREATED,
            id,
            user,
            createdAt = createdAt)
}

data class ProcessedJob(
        override val id: Id,
        override val user: User,
        val createdAt: Date,
        val filePath: String,
        val processedAt: Date,
        val pageCount: Int,
        val colorPageCount: Int
) : PrintJob() {
    override fun json() = PrintJobJson(
            PROCESSED,
            id,
            user,
            createdAt = createdAt,
            filePath = filePath,
            processedAt = processedAt,
            pageCount = pageCount,
            colorPageCount = colorPageCount)
}

data class PrintedJob(
        override val id: Id,
        override val user: User,
        val createdAt: Date,
        val filePath: String?,
        val processedAt: Date,
        val pageCount: Int,
        val colorPageCount: Int,
        val printerGroup: String,
        val finishedAt: Date
) : PrintJob() {
    override fun json() = PrintJobJson(
            PRINTED,
            id,
            user,
            createdAt = createdAt,
            filePath = filePath,
            processedAt = processedAt,
            pageCount = pageCount,
            colorPageCount = colorPageCount,
            printerGroup = printerGroup,
            finishedAt = finishedAt)
}

data class FailedJob(
        override val id: Id,
        override val user: User,
        val errorFlag: String,
        val finishedAt: Date
) : PrintJob() {
    override fun json() = PrintJobJson(
            FAILED,
            id,
            user,
            errorFlag = errorFlag,
            finishedAt = finishedAt)
}

/**
 * Model showing specific state of a print job.
 * A print job is expected to have a history of refunds,
 * with the most recent one reflecting its actual state.
 * If no refunds are found, then the job has never been refunded.
 */
data class PrintRefund(
        val id: Id,
        val refunder: User,
        val date: Date,
        val refund: Boolean
)
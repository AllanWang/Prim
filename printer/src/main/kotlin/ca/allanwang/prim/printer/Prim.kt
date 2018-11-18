package ca.allanwang.prim.printer

import ca.allanwang.prim.models.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.io.InputStream

object Prim : KoinComponent {

    private val printJobRepository: PrintJobRepository by inject()
    private val printerConfigs: PrinterConfiguration by inject()

    fun preparePrint(session: Session, jobName: String, printerGroup: Id): PrintJob? {
        val job = printJobRepository.create(session.user, jobName, printerGroup)
                ?: return null
        return if (QueueManager.hasPrinters(session.role, printerGroup)) {
            job
        } else {
            printJobRepository.updateFailed(job.id, FailedJob.NO_PRINTERS)
        }
    }

    fun print(session: Session, jobId: Id, stream: InputStream): PrintJob? {
        val job = printJobRepository.getById(jobId)?.takeIf { it is CreatedJob } ?: return run {
            stream.close()
            null
        }

        fun fail(flag: Flag): PrintJob? {
            stream.close()
            return printJobRepository.updateFailed(job.id, flag)
        }

        val tmpDir = printerConfigs.tmpDir

        if (!tmpDir.exists() && !tmpDir.mkdirs())
            return fail(FailedJob.IO_FAILURE)

        TODO()
    }

    fun print(job: PrintedJob) {
        // Check that file exists and is postscript
        // Send to printer
    }

}
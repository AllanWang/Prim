package ca.allanwang.prim.printer

import ca.allanwang.prim.models.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.io.File
import java.io.InputStream
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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

        val file = File(tmpDir, "$jobId.ps")

        try {
            file.copyFrom(stream)
        } catch (e: Exception) {
            return fail(FailedJob.IO_FAILURE)
        }

        val processedJob = printJobRepository.updateProcessed(jobId, file.absolutePath, 0, 0)
                ?: fail(FailedJob.DELETED_JOB)

        TODO()
    }

    fun print(job: PrintedJob) {
        // Check that file exists and is postscript
        // Send to printer
    }

    private fun File.copyFrom(input: InputStream, vararg options: CopyOption = arrayOf(StandardCopyOption.REPLACE_EXISTING)) {
        input.use {
            Files.copy(it, toPath(), *options)
        }
    }

}
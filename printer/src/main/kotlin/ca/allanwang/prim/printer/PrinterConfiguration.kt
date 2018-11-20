package ca.allanwang.prim.printer

import ca.allanwang.prim.models.*
import java.io.File
import java.io.InputStream

interface PrinterConfiguration {

    /**
     * Returns the list of printers to be considered for a print job
     * [printers] are already filtered so that none are [PrinterStatus.FLAG_DISABLED],
     * and all are in the appropriate printer group.
     *
     * The only consideration is the specific role of the person printing,
     * as that is configurable.
     *
     * Note that further information concerning which printers to use is decided
     * by the queue manager, which is why the job info is not provided here.
     */
    fun getCandidatePrinters(role: Role, printers: List<Printer>): List<Printer>

    /**
     * Called to fetch a load balancer if the name is not recognized by the current implementations.
     * Remember that this should return a new instance and not reuse an existing one.
     */
    fun createLoadBalancer(name: Flag): LoadBalancer

    /**
     * Given a processed job, return true if it should be printed.
     * At this stage, everything has been verified on Prim's end.
     * This is a good time to check for things like color printing or sufficient quota.
     */
    fun canPrint(printerId: Id, job: ProcessedJob): Validation

    /**
     * Temp directory that houses all the files for printing.
     */
    val tmpDir: File

    fun print(job: PrintedJob)

    /**
     * Decompresses the given input stream.
     * This should do the opposite of whatever compression was used when receiving the file.
     */
    fun decompress(stream: InputStream): InputStream = stream

}

class DefaultPrinterConfiguration : PrinterConfiguration {

    override fun getCandidatePrinters(role: Role, printers: List<Printer>): List<Printer> =
            printers.filter { it.flag == PrinterStatus.FLAG_ENABLED }

    override fun createLoadBalancer(name: Flag): LoadBalancer = LoadBalancer.fromName(LoadBalancer.DEFAULT)!!

    override fun canPrint(printerId: Id, job: ProcessedJob): Validation = Valid

    override val tmpDir: File = File("") // TODO pick better default

    override fun print(job: PrintedJob) {
        // TODO use slf4j
        println("Printed ${job.jobName}")
    }

}
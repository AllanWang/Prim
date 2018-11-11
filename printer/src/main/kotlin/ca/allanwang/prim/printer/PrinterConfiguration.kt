package ca.allanwang.prim.printer

import ca.allanwang.ktor.models.Printer
import ca.allanwang.ktor.models.PrinterStatus

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
    fun getCandidatePrinters(role: String, printers: List<Printer>): List<Printer>

}

class DefaultPrinterConfiguration : PrinterConfiguration {

    override fun getCandidatePrinters(role: String, printers: List<Printer>): List<Printer> =
            printers.filter { it.flag == PrinterStatus.FLAG_ENABLED }

}
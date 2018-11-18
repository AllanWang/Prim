package ca.allanwang.prim.printer

import ca.allanwang.prim.models.*

/**
 * Basic repository interface, where we can get and delete items
 */
interface Repository<K : Comparable<K>, M : Any> {

    /**
     * Gets a single item by its unique id.
     * Returns null if no item is found.
     */
    fun getById(id: K): M?

    /**
     * Deletes a single item by its unique id.
     * If the item is not found, then nothing happens.
     */
    fun deleteById(id: K)

    /**
     * Returns a list of items without any particular order.
     * Note that if the parameters are not modified, the full
     * list of items should be returned.
     * [limit] specifies the upper bound for the number of items to return when it is positive.
     * [offset] specifies the start index of the returned list relative to the full list;
     * this is useful for pagination.
     */
    fun getList(limit: Int = -1, offset: Int = 0): List<M>

    /**
     * Returns the number of items in the entire repository.
     */
    fun count(): Int

}

enum class Order {
    ASCENDING, DESCENDING
}

interface SessionRepository : Repository<Id, Session> {

    /**
     * Creates a new session for the user and role
     * This should automatically invalidate all preexisting sessions
     * with a different role
     * [expiresIn] is the time in ms for which the session will remain valid
     * Any expiration duration under 0 will go to a default value
     */
    fun create(user: User, role: String, expiresIn: Long = -1): Session?

    /**
     * Deletes session by unique user key.
     */
    fun deleteByUser(user: User)

    /**
     * Deletes session if expiration date has already passed.
     */
    fun deleteExpired()

}

interface PrintJobRepository : Repository<Id, PrintJob> {

    /**
     * Generates a new print job for a user.
     * This acts as a placeholder, and holds nothing more than a unique id.
     * If a job is in the created state for a long time, it should eventually fail.
     */
    fun create(user: User, printerGroup: Id): CreatedJob?

    /**
     * Attempts to update a previously created job to the processed stage.
     * This state will attach all the necessary info about the job to prepare its print process.
     * Note that this should only apply to a job that already exists and is in the [PrintJob.CREATED] phase
     */
    fun updateProcessed(id: Id, filePath: String, totalPageCount: Int, colorPageCount: Int): ProcessedJob?

    /**
     * Attempts to update a previously processed job to the printed stage.
     * This marks the job as complete, and will attach any remaining info.
     * Note that this should only apply to a job that already exists and is in the [PrintJob.PROCESSED] phase.
     */
    fun updatePrinted(id: Id, printer: Id): PrintedJob?

    /**
     * Attempts to update an existing job to the failed stage.
     * Note that this can be applied to a job from any state except for [PrintJob.PRINTED], as it is already a final stage.
     */
    fun updateFailed(id: Id, error: Flag): FailedJob?

    /**
     * Gets all the jobs specific to the provided printer id.
     * [order] is based on print time.
     */
    fun getListByPrinterGroup(printerGroup: Id, limit: Int = -1, offset: Int = 0, order: Order = Order.DESCENDING): List<PrintJob>

//    fun getListSortedByDate(limit: Int = -1, offset: Int = 0, order: Order = Order.DESCENDING): List<PrintedJob>

//    fun getListSortedByName(limit: Int = -1, offset: Int = 0, order: Order = Order.ASCENDING): List<PrintedJob>

    /**
     * Attempts to update a previously printed job to the new refund status.
     * Note that this should only apply to a job that already exists and is in the [PrintJob.PRINTED].
     * If the printed job exists, it will be returned with the newest refund status.
     */
    fun updateRefundStatus(id: Id, refunder: User, refund: Boolean): PrintedJob?

}

interface PrinterRepository : Repository<Id, Printer> {

    /**
     * Creates a new printer with the provided parameters.
     * [id] is a unique identifier for the printer.
     * [name] is the unique display name for the printer.
     * [group] is the group that the printer is a part of.
     *
     * TODO check if we want to take in [PrinterGroup] or just a String
     */
    fun create(id: Id, name: Name, group: PrinterGroup): Printer?

    /**
     * Get a list of printers from the given group
     */
    fun getList(group: PrinterGroup): List<Printer>

    /**
     * Set the printer to the provided flags.
     * Note that no checks are done about the previous state.
     * Regardless of whether the flag has changed, the current data will represent
     * the printer's state.
     * If the printer exists, the new model will be returned.
     */
    fun updateStatus(id: Id, user: User, flag: Flag, message: String): Printer?

}

interface PrinterGroupRepository : Repository<Id, PrinterGroup> {

    /**
     * Creates a new printer group with the provided parameters.
     * [id] is a unique identifier for the group
     * [name] is the unique display name for the printer.
     * [loadBalancer] is the name of the [LoadBalancer] to use.
     */
    fun create(id: Id, name: Name, loadBalancer: Flag = LoadBalancer.DEFAULT): PrinterGroup?

    /**
     * Attempts to switch the load balancer of selected printer group to the new one.
     * If successful, returns the printer group. Returns null otherwise.
     */
    fun updateLoadBalancer(id: Id, loadBalancer: Flag): PrinterGroup?

    /**
     * Retrieves a map of every printer group to its associated printers.
     * Note that if a printer has no associated group, it will not be shown here.
     */
    fun getAllPrinters(): Map<PrinterGroup, List<Printer>>

    /**
     * Given a group id, attempt to get the associated group
     * and its printers
     */
    fun getPrinter(group: Id): Pair<PrinterGroup, List<Printer>>?

}
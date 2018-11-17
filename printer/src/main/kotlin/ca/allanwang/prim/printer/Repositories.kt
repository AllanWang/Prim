package ca.allanwang.prim.printer

import ca.allanwang.prim.models.*

/**
 * Basic repository interface, where we can get and delete items
 */
interface Repository<M : Any> {

    /**
     * Gets a single item by its unique id.
     * Returns null if no item is found.
     */
    fun getById(id: Id): M?

    /**
     * Deletes a single item by its unique id.
     * If the item is not found, then nothing happens.
     */
    fun deleteById(id: Id)

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

/**
 * Repository with a default list output, without conditions.
 * Such lists can also be ordered.
 */
interface OrderedListRepository<M : Any> {

    /**
     * Similar to [Repository.getList], though there is an additional order constraint.
     * What the order means is dependent on the implementation.
     */
    fun getList(limit: Int = -1, offset: Int = 0, order: Order): List<M>

}

enum class Order {
    ASCENDING, DESCENDING
}

interface SessionRepository :
        Repository<Session> {

    /**
     * Creates a new session for the user and role
     * This should automatically invalidate all preexisting sessions
     * with a different role
     * [expiresIn] is the time in ms for which the session will remain valid
     * Any expiration duration under 0 will go to a default value
     */
    fun create(user: User, role: String, expiresIn: Long = -1): Session?

    fun deleteByUser(user: User)

    fun deleteExpired()

}

interface PrinterRepository :
        Repository<Printer> {

    fun create(name: String, group: PrinterGroup): Printer?

    /**
     * Get a list of printers from the given group
     */
    fun getList(group: PrinterGroup): List<Printer>

}

interface PrinterGroupRepository :
        Repository<PrinterGroup> {

    fun create(name: String, queueManager: Flag): PrinterGroup?

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
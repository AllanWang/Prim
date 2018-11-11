package ca.allanwang.prim.printer

import ca.allanwang.ktor.models.*
import ca.allanwang.prim.models.*

/**
 * Basic repository interface, where we can get and delete items
 */
interface Repository<M : Any> {

    fun getById(id: Id): M?

    fun deleteById(id: Id)

    fun getList(limit: Int = -1, offset: Int = 0): List<M>

}

/**
 * Repository with a default list output, without conditions.
 * Such lists can also be ordered.
 */
interface OrderedListRepository<M : Any> {

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
    fun create(user: User, role: String, expiresIn: Long = -1): Session

    fun deleteByUser(user: User)

    fun deleteExpired()

}

interface PrinterRepository :
        Repository<Printer> {

    fun create(name: String, group: PrinterGroup): Printer

    /**
     * Get a list of printers from the given group
     */
    fun getList(group: PrinterGroup): List<Printer>

}

interface PrinterGroupRepository :
        Repository<PrinterGroup> {

    fun create(name: String, queueManager: Flag): PrinterGroup


}
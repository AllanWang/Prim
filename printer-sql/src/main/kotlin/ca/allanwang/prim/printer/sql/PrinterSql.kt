package ca.allanwang.prim.printer.sql

import ca.allanwang.prim.models.IdModel
import ca.allanwang.prim.printer.*
import ca.allanwang.prim.printer.sql.repos.PrintJobRepositorySql
import ca.allanwang.prim.printer.sql.repos.PrinterGroupRepositorySql
import ca.allanwang.prim.printer.sql.repos.PrinterRepositorySql
import ca.allanwang.prim.printer.sql.repos.SessionRepositorySql
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module.module

/**
 * Exposed koin injection for printer repositories
 */
val sqlRepositoryModule = module {
    single<SessionRepository> { SessionRepositorySql }
    single<PrintJobRepository> { PrintJobRepositorySql }
    single<PrinterRepository> { PrinterRepositorySql }
    single<PrinterGroupRepository> { PrinterGroupRepositorySql }
}

internal const val ID_SIZE = 255
internal const val FLAG_SIZE = 64
internal const val NAME_SIZE = 64
internal const val USER_SIZE = 64
internal const val MESSAGE_SIZE = 255

/**
 * Base implementation of a repository, extended by sql queries.
 * Given the structure, a lot of helper methods can be provided with just an id table.
 */
abstract class SqlRepository<K : Comparable<K>, M : IdModel<K>, T : IdTable<K>>(val table: T) : Repository<K, M> {

    /**
     * Converts a row into model [M].
     * By default, the result will contain columns within [table].
     * However, this may be overridden through [retrieverFields].
     */
    protected abstract fun ResultRow.rowToModel(): M

    /**
     * Fieldset used by getter executions.
     * This may be overridden to add table joins.
     */
    open fun retrieverFields(): FieldSet = table

    override fun getById(id: K): M? = transaction {
        retrieverFields().select { table.id eq id }.firstOrNull()?.rowToModel()
    }

    override fun deleteById(id: K): Unit = transaction {
        table.deleteWhere { table.id eq id }
    }

    override fun getList(limit: Int, offset: Int): List<M> = transaction {
        retrieverFields().selectAll()
                .orderBy(table.id to SortOrder.ASC)
                .limit(limit, offset)
                .map { it.rowToModel() }
    }

    override fun count(): Int = transaction {
        table.selectAll().count()
    }

    /**
     * Attempts to insert an item with the provided id. If successful, outputs the saved data.
     * Note that the [body] should not contain any assignments to the id column.
     *
     * TODO pending on https://github.com/JetBrains/Exposed/issues/432
     */
    protected fun transactionInsert(id: K, body: T.(InsertStatement<Number>) -> Unit): M? = try {
        transaction {
            table.insert {
                body(it)
                it[table.id] = EntityID(id, table)
            }
            getById(id)
        }
    } catch (e: Exception) {
//        e.printStackTrace()
        null
    }

    /**
     * Checks if entry exists and returns null if it doesn't.
     * Otherwise, applies update, and returns the retrieved output.
     * By default the query condition, [where], checks for key matching.
     * It can be overridden to impose more constraints, though it should ultimately
     * match at most one item in most cases.
     */
    protected fun transactionUpdate(key: K,
                                    where: SqlExpressionBuilder.() -> Op<Boolean> = { table.id eq key },
                                    body: T.(UpdateStatement) -> Unit): M? = transaction {
        if (table.select(where).count() == 0) return@transaction null
        table.update(where, body = body)
        commit()
        getById(key)
    }

}
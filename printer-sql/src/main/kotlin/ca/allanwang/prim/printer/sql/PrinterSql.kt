package ca.allanwang.prim.printer.sql

import ca.allanwang.prim.printer.PrinterGroupRepository
import ca.allanwang.prim.printer.PrinterRepository
import ca.allanwang.prim.printer.Repository
import ca.allanwang.prim.printer.SessionRepository
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
    single<PrinterRepository> { PrinterRepositorySql }
    single<PrinterGroupRepository> { PrinterGroupRepositorySql }
}

internal const val ID_SIZE = 255
internal const val FLAG_SIZE = 64
internal const val NAME_SIZE = 64
internal const val USER_SIZE = 64
internal const val MESSAGE_SIZE = 255

abstract class PrimIdTable<K : Comparable<K>>(name: String) : Table(name) {
    abstract val id: Column<K>
}

abstract class SqlRepository<K : Comparable<K>, M : Any, T : IdTable<K>>(val table: T) : Repository<K, M> {

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
        retrieverFields().selectAll().map { it.rowToModel() }
    }

    override fun count(): Int = transaction {
        table.selectAll().count()
    }

    protected fun transactionInsert(body: T.(InsertStatement<Number>) -> Unit): M? = transaction {
        try {
            println("Assert")
            val ins = table.insert(body)
            println("INserted ${getList()}")
            val id = ins get table.id
            val data = getById(id!!.value)
            println("Data $data")
            data
        } catch (e: Exception) {
            println("Error $e")
            null
        }
    }

    /**
     * Checks if entry exists and returns null if it doesn't.
     * Otherwise, applies update, and returns the retrieved output.
     */
    protected fun transactionSingleUpdate(key: K,
                                          body: T.(UpdateStatement) -> Unit): M? = transaction {
        if (table.select { table.id eq key }.count() == 0) return@transaction null
        table.update({ table.id eq key }, body = body)
        commit()
        getById(key)
    }

}
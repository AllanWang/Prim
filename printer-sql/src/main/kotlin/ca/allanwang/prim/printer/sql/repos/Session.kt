package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.models.Id
import ca.allanwang.prim.models.Role
import ca.allanwang.prim.models.Session
import ca.allanwang.prim.models.User
import ca.allanwang.prim.printer.SessionRepository
import ca.allanwang.prim.printer.newId
import ca.allanwang.prim.printer.sql.FLAG_SIZE
import ca.allanwang.prim.printer.sql.ID_SIZE
import ca.allanwang.prim.printer.sql.SqlRepository
import ca.allanwang.prim.printer.sql.USER_SIZE
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object SessionTable : IdTable<String>("session") {
    override val id = varchar("id", ID_SIZE).clientDefault(::newId).entityId()
    val user = varchar("user", USER_SIZE)
    val role = varchar("role", FLAG_SIZE)
    val createdAt = datetime("created_at").clientDefault(DateTime::now)
    val expiresAt = datetime("expires_at")
}

private const val DEFAULT_EXPIRATION_DURATION = 1000 * 60 * 60 * 24 * 30L // a month

internal object SessionRepositorySql : SessionRepository,
        SqlRepository<Id, Session, SessionTable>(SessionTable) {

    override fun ResultRow.rowToModel(): Session = Session(
            id = this[table.id].value,
            user = this[table.user],
            role = this[table.role],
            createdAt = this[table.createdAt].toDate(),
            expiresAt = this[table.expiresAt].toDate()
    )

    override fun create(user: User, role: Role, expiresIn: Long): Session? = transaction {
        table.deleteWhere { (table.user eq user) and (table.role neq role) }
        transactionInsert(newId()) {
            it[this.user] = user
            it[this.role] = role
            it[this.expiresAt] = DateTime.now().plus(if (expiresIn > 0) expiresIn else DEFAULT_EXPIRATION_DURATION)
        }
    }

    override fun deleteByUser(user: User): Unit = transaction {
        table.deleteWhere { table.user eq user }
    }

    override fun deleteExpired(): Unit = transaction {
        table.deleteWhere { table.expiresAt lessEq DateTime.now() }
    }

}

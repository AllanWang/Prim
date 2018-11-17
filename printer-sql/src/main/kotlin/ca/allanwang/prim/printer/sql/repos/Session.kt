package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.models.Id
import ca.allanwang.prim.models.Session
import ca.allanwang.prim.models.User
import ca.allanwang.prim.printer.SessionRepository
import ca.allanwang.prim.printer.sql.FLAG_SIZE
import ca.allanwang.prim.printer.sql.ID_SIZE
import ca.allanwang.prim.printer.sql.USER_SIZE
import ca.allanwang.prim.printer.sql.newId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object SessionTable : Table("session") {
    val id = varchar("id", ID_SIZE).primaryKey().clientDefault(::newId)
    val user = varchar("user", USER_SIZE)
    val role = varchar("role", FLAG_SIZE)
    val createdAt = datetime("created_at").clientDefault(DateTime::now)
    val expiresAt = datetime("expires_at")
}

private const val DEFAULT_EXPIRATION_DURATION = 1000 * 60 * 60 * 24 * 30L // a month

internal object SessionRepositorySql : SessionRepository {

    private fun ResultRow.toSession(): Session = Session(
            id = this[SessionTable.id],
            user = this[SessionTable.user],
            role = this[SessionTable.role],
            createdAt = this[SessionTable.createdAt].toDate(),
            expiresAt = this[SessionTable.expiresAt].toDate()
    )

    override fun getById(id: Id): Session? = transaction {
        SessionTable.select { SessionTable.id eq id }.firstOrNull()?.toSession()
    }

    override fun deleteById(id: Id): Unit = transaction {
        SessionTable.deleteWhere { SessionTable.id eq id }
    }

    override fun getList(limit: Int, offset: Int): List<Session> = transaction {
        SessionTable.selectAll().limit(limit, offset = offset).map { it.toSession() }.toList()
    }

    override fun create(user: User, role: String, expiresIn: Long): Session? = transaction {
        SessionTable.deleteWhere { (SessionTable.user eq user) and (SessionTable.role neq role) }
        val id = SessionTable.insert {
            it[SessionTable.user] = user
            it[SessionTable.role] = role
            it[SessionTable.expiresAt] = DateTime.now().plus(if (expiresIn > 0) expiresIn else DEFAULT_EXPIRATION_DURATION)
        } get SessionTable.id ?: return@transaction null
        getById(id)
    }

    override fun deleteByUser(user: User): Unit = transaction {
        SessionTable.deleteWhere { SessionTable.user eq user }
    }

    override fun deleteExpired(): Unit = transaction {
        SessionTable.deleteWhere { SessionTable.expiresAt lessEq DateTime.now() }
    }

    override fun count(): Int = transaction {
        SessionTable.selectAll().count()
    }

}

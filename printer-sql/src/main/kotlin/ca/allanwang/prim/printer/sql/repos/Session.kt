package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.ktor.models.Id
import ca.allanwang.ktor.models.Session
import ca.allanwang.ktor.models.User
import ca.allanwang.prim.printer.SessionRepository
import ca.allanwang.prim.printer.sql.newId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime


class SessionRepositorySql : SessionRepository {

    private fun ResultRow.toSession(): Session = Session(
            id = Id(this[SessionTable.id]),
            user = User(this[SessionTable.user]),
            role = this[SessionTable.role],
            createdAt = this[SessionTable.createdAt].toDate(),
            expiresAt = this[SessionTable.expiresAt].toDate()
    )

    override fun getById(id: Id): Session? = transaction {
        SessionTable.select { SessionTable.id eq id.value }.firstOrNull()?.toSession()
    }

    override fun deleteById(id: Id): Unit = transaction {
        SessionTable.deleteWhere { SessionTable.id eq id.value } != 0
    }

    override fun getList(limit: Int, offset: Int): List<Session> = transaction {
        SessionTable.selectAll().limit(limit, offset = offset).map { it.toSession() }.toList()
    }

    override fun create(user: User, role: String): Session = transaction {
        val id = SessionTable.insert {
            it[SessionTable.user] = user.value
            it[SessionTable.role] = role
        } get SessionTable.id
        getById(Id(id!!))!!
    }

    override fun deleteByUser(user: User): Unit = transaction {
        SessionTable.deleteWhere { SessionTable.user eq user.value } != 0
    }

    override fun deleteExpired(): Unit = transaction {
        SessionTable.deleteWhere { SessionTable.expiresAt lessEq DateTime.now() } != 0
    }

}

object SessionTable : Table() {
    val id = varchar("id", 255).primaryKey().clientDefault(::newId)
    val user = varchar("user", 64).uniqueIndex()
    val role = varchar("role", 64)
    val createdAt = datetime("created_at").clientDefault(DateTime::now)
    val expiresAt = datetime("expires_at")
}
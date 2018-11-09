package ca.allanwang.prim.printer

import ca.allanwang.ktor.models.Id
import ca.allanwang.ktor.models.Session
import ca.allanwang.ktor.models.User

interface Repository<M : Any> {

    fun getById(id: Id): M?

    fun deleteById(id: Id): Boolean

}

interface MutationRepository<M : Any> {

    fun save(model: M): Boolean

}

interface SessionRepository : Repository<Session>, MutationRepository<Session> {

    fun deleteByUser(user: User)

    fun deleteExpired(): Boolean

}
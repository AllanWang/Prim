package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.printer.repos.SessionTestBase
import ca.allanwang.prim.printer.sql.SqlExtension
import ca.allanwang.prim.printer.sql.sqlRepositoryModule
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionExtension : SqlExtension(
        tables = listOf(SessionTable),
        modules = listOf(sqlRepositoryModule))

@ExtendWith(SessionExtension::class)
class SessionTest : SessionTestBase() {

    @Test
    fun `create and get 2`() {
        println("Create and get")
        val session = createSession(0)!!
        assertEquals("testUser0", session.user.value)
        assertEquals("testRole0", session.role)

        assertEquals(session, sessionRepository.getById(session.id))
    }

}
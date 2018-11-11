package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.models.User
import ca.allanwang.prim.printer.SessionRepository
import ca.allanwang.prim.printer.sql.repos.SessionTable
import ca.allanwang.prim.printer.sql.sqlRepositoryModule
import ca.allanwang.prim.printer.sql.withTables
import org.jetbrains.exposed.sql.selectAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.standalone.StandAloneContext.closeKoin
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionTest : KoinTest {

    private val sessionRepository: SessionRepository by inject()

    @Before
    fun before() {
        startKoin(listOf(sqlRepositoryModule))
    }

    @After
    fun after() {
        closeKoin()
    }

    private fun createSession(key: Int = 0,
                              user: String = "testUser$key",
                              role: String = "testRole$key",
                              expiresIn: Long = 1000000) =
            sessionRepository.create(user = User(user), role = role, expiresIn = expiresIn)


    @Test
    fun `create and get`() = withTables(SessionTable) {
        val session = createSession(0)
        assertEquals("testUser0", session.user.value)
        assertEquals("testRole0", session.role)

        assertEquals(session, sessionRepository.getById(session.id))
    }

    @Test
    fun `create should delete invalid sessions`() = withTables(SessionTable) {
        val testUser = "testUser"
        val session = createSession(1, user = testUser)
        createSession(1, user = testUser)
        val sessionOtherUser = createSession(2)
        assertEquals(3, SessionTable.selectAll().count())
        createSession(2, user = testUser)
        assertEquals(2, SessionTable.selectAll().count())
        assertNull(sessionRepository.getById(session.id),
                "Session not invalidated when new one with different role was added")
        assertNotNull(sessionRepository.getById(sessionOtherUser.id),
                "Session invalidation removed a session from a different user")
    }

    @Test
    fun `negative expiresIn should have positive default`() = withTables(SessionTable) {
        val session = createSession(expiresIn = -1)
        assertTrue(session.createdAt < session.expiresAt,
                "No expiration default exists; if negative value is passed, the session should have some positive validity duration")
    }

}
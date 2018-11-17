package ca.allanwang.prim.printer.repos

import ca.allanwang.prim.models.User
import ca.allanwang.prim.printer.SessionRepository
import org.koin.standalone.inject
import kotlin.test.*

abstract class SessionTestBase : TestBase() {

    val sessionRepository: SessionRepository by inject()


    fun createSession(key: Int = 0,
                      user: String = "testUser$key",
                      role: String = "testRole$key",
                      expiresIn: Long = 1000000) =
            sessionRepository.create(user = User(user), role = role, expiresIn = expiresIn)


    @Test
    fun `create and get`() {
        println("Create and get")
        val session = createSession(0)!!
        assertEquals("testUser0", session.user.value)
        assertEquals("testRole0", session.role)

        assertEquals(session, sessionRepository.getById(session.id))
    }

    @Ignore
    @Test
    fun `create should delete invalid sessions`() {
        val testUser = "testUser"
        val session = createSession(1, user = testUser)!!
        createSession(1, user = testUser)
        val sessionOtherUser = createSession(2)!!
        assertEquals(3, sessionRepository.count())
        createSession(2, user = testUser)
        assertEquals(2, sessionRepository.count())
        assertNull(sessionRepository.getById(session.id),
                "Session not invalidated when new one with different role was added")
        assertNotNull(sessionRepository.getById(sessionOtherUser.id),
                "Session invalidation removed a session from a different user")
    }

    @Ignore
    @Test
    fun `negative expiresIn should have positive default`() {
        val session = createSession(expiresIn = -1)!!
        assertTrue(session.createdAt < session.expiresAt,
                "No expiration default exists; if negative value is passed, the session should have some positive validity duration")
    }

}
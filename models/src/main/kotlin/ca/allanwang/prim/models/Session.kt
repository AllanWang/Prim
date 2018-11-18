package ca.allanwang.prim.models

import java.util.*

/**
 * Association between a user and a token (id).
 * Each session is also associated with a role.
 * For safety, we will always require an expiration date,
 * which will be the latest time we will accept the session
 * during purges.
 */
data class Session(
        val user: User,
        val id: Id,
        val role: Role,
        val createdAt: Date,
        val expiresAt: Date
)
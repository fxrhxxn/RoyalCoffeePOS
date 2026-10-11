package com.royalcoffee.pos.model

/** Immutable login snapshot; never carries authentication credentials. */
data class AuthenticatedUser(
    val id: Long,
    val fullName: String,
    val role: UserRole,
    val sessionId: String = java.util.UUID.randomUUID().toString()
)

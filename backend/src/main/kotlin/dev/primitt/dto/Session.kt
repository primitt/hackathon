package dev.primitt.dto

data class SessionInput(val uuid: String, val sessionId: String)

data class SessionResponse(val username: String, val success: Boolean, val message: String)
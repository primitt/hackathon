package dev.primitt.dto

data class LoginInput(val username: String, val password: String)

data class LoginResponse(val success: String, val sessionId: String, val uuid: String)
package dev.primitt.dto

data class User(val username: String, val password: String)

data class RegisterResponse(val success: Boolean, val sessionId: String, val uuid: String)
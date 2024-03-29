package dev.primitt.dto

data class User(val username: String, val password: String)

data class RegisterResponse(val success: String, val sessionId: String, val uuid: String)
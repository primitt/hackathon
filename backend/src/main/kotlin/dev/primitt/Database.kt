package dev.primitt

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val name = varchar("name", 16)
    val pass = varchar("password", 255)
    val uuid = varchar("uuid", 36)
    val restrictions = varchar("restrictions", 1000).nullable()
    val preferences = varchar("preferences", 1000).nullable()
    val recipes = varchar("recipes", 1000).nullable()
}

object Sessions : Table() {
    val uuid = varchar("uuid", 36)
    val sessionId = varchar("session_id", 36)
}


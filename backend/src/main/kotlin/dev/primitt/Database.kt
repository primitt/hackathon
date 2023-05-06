package dev.primitt

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val name = varchar("name", 16)
    val pass = varchar("password", 40)
    val restrictions = varchar("restrictions", 500)
    val preferences = varchar("preferences", 500)
}


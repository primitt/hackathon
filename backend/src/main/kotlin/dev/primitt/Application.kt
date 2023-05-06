package dev.primitt

import io.ktor.server.application.*
import dev.primitt.plugins.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureRouting()
    Database.connect(
        "jdbc:postgresql://ep-ancient-credit-653271.us-east-2.aws.neon.tech/neondb?user=26mittrapriansh&password=TFA7MZJN0pSL",
        driver = "org.postgresql.Driver")
    transaction {
        SchemaUtils.create(Users)
    }
}

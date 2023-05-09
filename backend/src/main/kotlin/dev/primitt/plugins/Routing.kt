package dev.primitt.plugins

import dev.primitt.Sessions
import dev.primitt.Users
import dev.primitt.dto.*
import dev.primitt.gson
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*


@OptIn(DelicateCoroutinesApi::class)
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/api/{args}") {
            val args = call.parameters["args"]
            val received = call.receive<String>()
            when (args) {
                "register" -> {
                    transaction {
                        println(received)
                        val user = gson.fromJson(received, User::class.java)
                        val users = Users.select(Users.name eq user.username)
                        if (users.count().toInt() == 0) {

                            // <------- creates new UUID's and session ids until unique ones are found ------->
                            var generatedNewUUID = false
                            var generatedNewSessionId = false

                            lateinit var generatedUuid: String
                            lateinit var generatedSessionId: String
                            do {
                                generatedUuid = UUID.randomUUID().toString()
                                val uuidExists = Users.select { Users.uuid eq generatedUuid }.count() > 0
                                if (!uuidExists) {
                                    generatedNewUUID = true
                                }
                            } while (!generatedNewUUID)

                            do {
                                generatedSessionId = UUID.randomUUID().toString()
                                val sessionIdExists = Sessions.select { Sessions.sessionId eq generatedSessionId }.count() > 0
                                if (!sessionIdExists) {
                                    generatedNewSessionId = true
                                }
                            } while (!generatedNewSessionId)
                            // ^<------- creates new UUID's and session ids until unique ones are found ------->^

                            Users.insert {
                                it[name] = user.username
                                it[pass] = user.password
                                it[uuid] = generatedUuid
                            }
                            Sessions.insert {
                                it[sessionId] = generatedSessionId
                                it[uuid] = generatedUuid
                            }
                            runBlocking {
                                call.respond(
                                    gson.toJson(
                                        RegisterResponse(
                                            "true",
                                            generatedSessionId,
                                            generatedUuid
                                        )
                                    )
                                )
                                println("Register, user created")
                            }
                        } else {
                            runBlocking {
                                call.respond(
                                    gson.toJson(
                                        RegisterResponse(
                                            "false",
                                            "",
                                            ""
                                        )
                                    )
                                )
                                println("Register, user already exists")
                            }
                        }
                    }
                }

                "session" -> {
                    transaction {
                        val receive = runBlocking { call.receive<String>() }
                        val sessionInput = gson.fromJson(receive, SessionInput::class.java)
                        val sessions = Sessions.select(Sessions.sessionId eq sessionInput.sessionId)
                        if (sessions.count().toInt() == 0) {
                            runBlocking {
                                call.respond(
                                    gson.toJson(
                                        SessionResponse(
                                            "",
                                            "false",
                                            "Invalid session ID"
                                        )
                                    )
                                )
                                println("Invalid session ID")
                            }
                        } else {
                            val user = Users.select(Users.uuid eq sessions.first()[Sessions.uuid]).first()
                            runBlocking {
                                call.respond(
                                    gson.toJson(
                                        SessionResponse(
                                            user[Users.name],
                                            "true",
                                            ""
                                        )
                                    )
                                )
                                println("Valid session ID, response sent")
                            }
                        }
                    }
                }

                "login" -> {
                    val receivedObject = gson.fromJson(received, LoginInput::class.java)

                    transaction {
                        val loginUser = Users.select { Users.name eq receivedObject.username }.firstOrNull()
                        if (loginUser == null) {
                            runBlocking {
                                call.respond(
                                    gson.toJson(
                                        LoginResponse(
                                            "false",
                                            "",
                                            ""
                                        )
                                    )
                                )
                                println("Login failed")
                            }
                            return@transaction
                        }
                        if (loginUser[Users.pass] == receivedObject.password) {
                            val uuid = loginUser[Users.uuid]
                            val sessionId = Sessions.select { Sessions.uuid eq uuid }.first()[Sessions.sessionId]
                            runBlocking {
                                call.respond(
                                    gson.toJson(
                                        LoginResponse(
                                            "true",
                                            sessionId,
                                            uuid
                                        )
                                    )
                                )

                                println("Login successful")
                            }
                        } else {
                            runBlocking {
                                call.respond(
                                    gson.toJson(
                                        LoginResponse(
                                            "false",
                                            "",
                                            ""
                                        )
                                    )
                                )
                                println("Login failed")
                            }
                        }
                    }
                }

                "survey" -> {
                    val receivedObject = gson.fromJson(received, PreferenceInput::class.java)

                    transaction {
                        val affected = Users.update({ Users.uuid eq receivedObject.uuid }) {
                            it[preferences] = gson.toJson(receivedObject.toServerPreference())
                        }

                        if (affected == 1) {
                            runBlocking { call.respond("success") }
                        } else {
                            runBlocking { call.respond("failure, no users updated") }
                        }
                    }
                }


            }
        }
    }
}

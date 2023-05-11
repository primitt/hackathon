package dev.primitt.plugins

import com.cjcrafter.openai.OpenAI
import com.cjcrafter.openai.chat.ChatMessage
import com.cjcrafter.openai.chat.ChatRequest
import com.cjcrafter.openai.chat.ChatUser
import dev.primitt.*
import dev.primitt.dto.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.engine.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.serialization.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*
import java.util.List
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.arrayListOf
import kotlin.collections.first
import freemarker.cache.*
import io.ktor.server.freemarker.*
import kotlin.collections.firstOrNull
import java.security.NoSuchAlgorithmException
import java.math.BigInteger
import java.security.MessageDigest
import java.net.HttpCookie


@OptIn(DelicateCoroutinesApi::class)
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(FreeMarkerContent("index.html", mapOf("data" to "none"), ""))
        }
        get("/login") {
            call.respond(FreeMarkerContent("login.html", mapOf("data" to "none"), ""))
        }
        get("/signup") {
            call.respond(FreeMarkerContent("signup.ftl", mapOf("data" to "none"), ""))
        }
        post("/signup") {
            val form = call.receiveParameters()
            transaction {
                val users = Users.select(Users.name eq form["username"]!!)
                if (form["password"] == form["rpassword"]) {
                    if (form["username"]!!.length < 17){
                        if (form["password"]!!.length > 7){
                            if (users.count().toInt() == 0){
                                var genUUID = UUID.randomUUID().toString()
                                var genSessionId = UUID.randomUUID().toString()
                                var hashedPsw: String = sha256(form["password"]!!).toString()
                                    Users.insert{
                                        it[name] = form["username"]!!
                                        it[pass] = hashedPsw
                                        it[uuid] = genUUID
                                    }
                                    Sessions.insert {
                                        it[sessionId] = genSessionId
                                        it[uuid] = genUUID
                                    }
                                runBlocking {
                                    call.response.cookies.append(Cookie("session", genSessionId, maxAge = 2592000))
                                    call.response.cookies.append(Cookie("uuid", genUUID, maxAge = 2592000))
                                    call.respondRedirect("/")
                                }
                            }
                            else{
                                runBlocking{
                                    call.respond(FreeMarkerContent("signup.ftl", mapOf("message" to "This username is already taken! Please chose a different one!"), ""))
                                }
                            }
                        }
                        else{
                            runBlocking {
                                call.respond(FreeMarkerContent("signup.ftl", mapOf("message" to "Your password has to be over 8 characters!"), ""))
                            }
                        }
                    }
                    else{
                        runBlocking {
                            call.respond(FreeMarkerContent("signup.ftl", mapOf("message" to "Your username is too long man, it has to be less than 16 characters!"), ""))
                        }
                    }
                }
                else{
                    runBlocking{
                        call.respond(FreeMarkerContent("signup.ftl", mapOf("message" to "Your password dont seem to match, please fix that! :3"), ""))
                    }
                }
            }
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
                        val sessionInput = gson.fromJson(received, SessionInput::class.java)
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

                "search" -> {
                    val receivedObject = gson.fromJson(received, SearchInput::class.java)
                    transaction {
                        val prefs = gson.fromJson(
                            Users.select { Users.uuid eq receivedObject.uuid }.first()[Users.preferences],
                            ServerPreference::class.java
                        )

                        runBlocking {
                            call.respond(search(
                                receivedObject.searchquery,
                                arrayOf(prefs.diet),
                                prefs.allergies,
                                receivedObject.preptime
                            ))
                        }
                    }
                }

                "recipe" -> {
                    call.respond(getIngredientInfo(gson.fromJson(received, RecipeInfo::class.java).id))
                }

                "fyp" -> {
                    val user = Users.select(Users.uuid eq received).first()
                    val prefs = gson.fromJson(user[Users.preferences], ServerPreference::class.java)
                    val recipes = arrayListOf<String>()
                    getIngredientInfo(gson.fromJson(received, RecipeInfo::class.java).id)
                }
            }
        }
    }
}

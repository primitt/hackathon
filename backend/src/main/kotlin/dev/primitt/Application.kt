package dev.primitt

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import dev.primitt.plugins.*
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
    import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureRouting()
    Database.connect(
        "jdbc:postgresql://ep-ancient-credit-653271.us-east-2.aws.neon.tech/neondb?user=26mittrapriansh&password=TFA7MZJN0pSL",
        driver = "org.postgresql.Driver"
    )
    transaction {
        SchemaUtils.create(Users, Sessions)
    }
}

fun complexSearch(query: String, diet: String, intolerances: String) {
    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.spoonacular.com/recipes/complexSearch?apiKey=5a5bb29a98ef4762917c9e17af5553f2&query=$query&diet=$diet&intolerances=$intolerances"))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
}

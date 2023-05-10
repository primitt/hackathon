package dev.primitt

import com.apollographql.apollo3.ApolloClient
import dev.primitt.graphql.queryLaunchList
import dev.primitt.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import freemarker.cache.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://apollo-fullstack-tutorial.herokuapp.com/graphql")
    .build()

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

    runBlocking {
        println(queryLaunchList().data)
    }
    install (FreeMarker){
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
}
val key = "6e01572b168f4ecc81786cf761aa265a"
fun getIngredientInfo(id: Int): String {
    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.spoonacular.com/recipes/$id/information?apiKey=$key"))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    println(response.headers().firstValue("X-API-Quota-Used"))
    return response.body()
}
var searchResults = ""
fun search(query: String, inputDiets: Array<String>, inputIntolerances: Array<String>, prepTime: Int): String {
    var diets = ""
    var intolerances = ""

    if (inputDiets.size > 1) {
        for (diet in inputDiets) {
            diets += if (inputDiets.size - 1 == inputDiets.indexOf(diet)) diet
            else "$diet,"
        }
    } else diets = inputDiets[0]

    if (inputIntolerances.size > 1) {
        for (intolerance in inputIntolerances) {
            intolerances += if (inputIntolerances.size - 1 == inputIntolerances.indexOf(intolerance)) intolerance
            else "$intolerance,"
        }
    } else intolerances = inputIntolerances[0]

    val validatedDiets = diets.replace(" ", "%20")
    val validatedIntolerances = intolerances.replace(" ", "%20")
    val validatedQuery = query.replace(" ", "%20")

    println(validatedQuery)
    println(validatedDiets)
    println(validatedIntolerances)
    println(prepTime)

    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.spoonacular.com/recipes/complexSearch?apiKey=$key&query=$validatedQuery&diet=$validatedDiets&intolerances=$validatedIntolerances&maxReadyTime=$prepTime"))
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    println(response.headers().firstValue("X-API-Quota-Used"))
    return response.body()
}

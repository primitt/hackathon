package dev.primitt

import org.json.JSONObject
import dev.primitt.plugins.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI
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

fun getIngredientInfo(id: Int): String {
    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.spoonacular.com/recipes/$id/information?apiKey=5a5bb29a98ef4762917c9e17af5553f2"))
        .build()

    return client.send(request, HttpResponse.BodyHandlers.ofString()).body()

}
var searchResults = ""
fun search(query: String, inputDiets: Array<String>, inputIntolerances: Array<String>, prepTime: Int): String {
    var diets = ""
    var intolerances = ""
    for (diet in inputDiets) {
        diets = "$diets,$diet"
    }
    for (intolerance in inputIntolerances) {
        intolerances = "$intolerances,$intolerance"
    }

    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.spoonacular.com/recipes/complexSearch?apiKey=5a5bb29a98ef4762917c9e17af5553f2&query=$query&diet=$diets&intolerances=$intolerances&maxReadyTime=$prepTime"))
        .build()


    searchResults = client.send(request, HttpResponse.BodyHandlers.ofString()).body()
    return searchResults
}
fun getIdFromImage(url:String): Int {
    val json = JSONObject(searchResults)
    var results = json.getJSONArray("results")
    var id = 0
    for (result in results) {
        var resultJSONObject = JSONObject(result)
        if (resultJSONObject.getString("image").equals(url)) {
            id = resultJSONObject.getInt("id")
        }
    }
    return id
}

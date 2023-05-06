package dev.primitt

import com.google.gson.Gson

val gson = Gson()

class Server {

    fun initializeServer(): Server {
        return Server()
    }

    private constructor() : super() {

    }
}
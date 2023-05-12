
package dev.primitt

import com.cjcrafter.openai.OpenAI
import com.cjcrafter.openai.chat.ChatMessage
import com.cjcrafter.openai.chat.ChatRequest
import com.cjcrafter.openai.chat.ChatUser

// Use a mutable (modifiable) list! Always! You should be reusing the
// ChatRequest variable, so in order for a conversation to continue
// you need to be able to modify the list.

// Use a mutable (modifiable) list! Always! You should be reusing the
// ChatRequest variable, so in order for a conversation to continue
// you need to be able to modify the list.
val messages: MutableList<ChatMessage> = arrayListOf()

// ChatRequest is the request we send to OpenAI API. You can modify the
// model, temperature, maxTokens, etc. This should be saved, so you can
// reuse it for a conversation.

// ChatRequest is the request we send to OpenAI API. You can modify the
// model, temperature, maxTokens, etc. This should be saved, so you can
// reuse it for a conversation.
val request = ChatRequest.builder()
    .model("gpt-3.5-turbo")
    .messages(messages).build()

// Loads the API key from the .env file in the root directory.
// You should never put your API keys in code, keep your key safe!

// Loads the API key from the .env file in the root directory.
// You should never put your API keys in code, keep your key safe!
val chatKey: String = "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"
val openai = OpenAI(chatKey)

fun proompt(message: String) {
    messages.add(ChatMessage(ChatUser.USER, message))

    // Use the OpenAI API to generate a response to the current
    // conversation. Print the resulting message.
    val response = openai.createChatCompletion(request)
    println(
        """
            
            ${response[0].message.content}
            """.trimIndent()
    )

    // Save the generated message to the conversational memory. It is
    // crucial to save this message, otherwise future requests will be
    // confused that there was no response.
    messages.add(response[0].message)
}

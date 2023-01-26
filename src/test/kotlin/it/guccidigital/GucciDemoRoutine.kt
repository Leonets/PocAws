package it.guccidigital
import it.guccidigital.playground.Item
import it.guccidigital.playground.Token
import kotlinx.coroutines.*
import org.http4k.client.JavaHttpClient
import kotlin.coroutines.suspendCoroutine
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response

fun main() = runBlocking { // this: CoroutineScope
    launch { // launch a new coroutine and continue
        delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
        println("World!") // print after delay

        val token = preparePost()
        val item: Item = Item();
        val post = submitPost(token, item)
        processGet(item)
    }
    println("Hello") // main coroutine continues while a previous one is delayed
}

fun processGet(post: Any) {
    TODO("Not yet implemented")
}

fun submitPost(token: Any, item: Any): Any {
    TODO("Not yet implemented")
}

suspend fun preparePost(): Token {
    // makes a request and suspends the coroutine
    val client: HttpHandler = JavaHttpClient()
    val response: Response = client(Request(Method.GET, "http://localhost:9000/ping"))
    println(" ouput " + response.bodyString() )

    return suspendCoroutine { /* ... */ }
}
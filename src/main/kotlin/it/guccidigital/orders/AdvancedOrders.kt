package it.guccidigital.orders

import createBucketAws
import it.guccidigital.*
import it.guccidigital.models.Order
import kotlinx.coroutines.*
import org.http4k.core.*
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import java.lang.Thread.currentThread

fun main() {
    val printingApp: HttpHandler = PrintRequest().then(app)

    val server = printingApp.asServer(SunHttp(9000)).start()
    println("Server started on " + server.port())

    println("Create bucket ")
    //create gucci bucket
    runBlocking {
        createBucketAws(bucketName)
        //start a loop over the marketing queue
        alwaysReceiveMarketingAlerts()
        //perchè se questa function sopra viene eseguita prima dello start del server
        //allora lo start non viene mai eseguito (eppure dovrebbe essere suspend)
        manageOrders()
    }
}


fun CoroutineScope.alwaysReceiveMarketingAlerts() = launch {
    repeatUntilCancelled {
        val receiveRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueMarketingUrl)
            .waitTimeSeconds(20)
            .maxNumberOfMessages(10)
            .build()

//        val messages = sqsClient.receiveMessage(receiveRequest).await().messages()
//        println("${Thread.currentThread().name} Retrieved ${messages.size} messages")

//        messages.forEach {
//            channel.send(it)
//        }
    }
}

suspend fun CoroutineScope.repeatUntilCancelled(block: suspend () -> Unit) {
    while (isActive) {
        try {
            block()
            yield()
        } catch (ex: CancellationException) {
            println("coroutine on ${currentThread().name} cancelled")
        } catch (ex: Exception) {
            println("${currentThread().name} failed with {$ex}. Retrying...")
            ex.printStackTrace()
        }
    }
    println("coroutine on ${currentThread().name} exiting")
}

private fun getJsonOrder(singleMessage: Message): Order? {
    println(" Json syntax -> " + singleMessage.body())
    return gson.fromJson(singleMessage.body(), Order::class.java)
}


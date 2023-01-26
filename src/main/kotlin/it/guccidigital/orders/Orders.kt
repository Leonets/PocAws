package it.guccidigital.orders

import aws.smithy.kotlin.runtime.content.ByteStream
import createBucket
import it.guccidigital.*
import it.guccidigital.models.Orders
import kotlinx.coroutines.runBlocking
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import putObjectStream

//definizione delle routes
val app: HttpHandler = routes(
    //register some routes
    "/ping" bind GET to {
        Response(OK).body("pong")
    },

    //register some routes
    "/orders" bind Method.POST to {
        println(message = " payload  " + it.toMessage())
        //save payload in the bucket and then process it
        runBlocking {
//            savePayload(it = it)
            process(it = it)
        }
        Response(OK).body("Orders received")
    }
)

suspend fun savePayload(it: Request) {
    //TODO duplicate code
    val ordersLens = Body.auto<Orders>().toLens()
    // extract the body from the message
    val extractedMessage: Orders = ordersLens(it)
    //sending data to the bucket
    val dataToSent: String = gson.toJson(extractedMessage)
    println(" sending data to the bucket " + dataToSent)
    //put the payload in the bucket with a random key (json sintax)
    putObjectStream(bucketName, randomStringByJavaRandom(), ByteStream.fromBytes(dataToSent.toByteArray()))
    println(" object saved to bucket " )
}

suspend fun process(it: Request) {
    val ordersLens = Body.auto<Orders>().toLens()
    // extract the body from the message
    val extractedMessage: Orders = ordersLens(it)

    extractedMessage.value.forEach { it ->
        //send to a SQS queue
        sendMessages(queueMarketingUrl, gson.toJson(it))
        //send to a SNS topic
        pubTopic(topicArnVal, it.toString())
    }
}


fun main() {
    val printingApp: HttpHandler = PrintRequest().then(app)

    println("Create bucket ")
    //create gucci bucket
    runBlocking {
        createBucket(it.guccidigital.bucketName)
    }

    val server = printingApp.asServer(SunHttp(9000)).start()

    println("Server started on " + server.port())
}

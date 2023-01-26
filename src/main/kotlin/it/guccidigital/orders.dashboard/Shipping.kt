package it.guccidigital.orders

import aws.sdk.kotlin.services.s3.model.Object
import com.google.gson.Gson
import extractShippingDetails
import getObject
import it.guccidigital.models.Order
import it.guccidigital.models.Orders
import it.guccidigital.bucketName
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

val gson = Gson()

val appDashboard: HttpHandler = routes(

    //register some routes
    "/orders/shipping" bind Method.GET to {
        println(message = " extract and show dashboard " )

        val result = runBlocking {
            //extract objects from s3
            extractShippingDetails(bucketName).contents?.forEach {it: Object ->

                it.key?.let { it1 -> getObject(bucketName, it1,"temp."+it.key) }
                val content: String = readFileContents(File("temp."+it.key))
                println(" read again contents from saved file   = " + content)
                //for each object extract the destination country
                gson.fromJson(content , Orders::class.java).value.forEach { it: Order ->
                    println(" destination country = " + it.destinationCountry)
                }
                //TODO trasformare in map()
            }
        }
        //BiDiBodyLens , set in the body
        Response(OK).body("TO BE CONSTRUCTED")
    }
)


fun main() {
    val printingApp: HttpHandler = PrintRequest().then(appDashboard)
    val server = printingApp.asServer(SunHttp(9001)).start()
    //start a loop over the queue with a thread pool for long polling
    runBlocking {
            receive()
    }
    println("Server started on " + server.port())
}

suspend fun receive() {
    val worker = Executors.newSingleThreadScheduledExecutor()
    val counter = AtomicInteger(0)
    worker.scheduleAtFixedRate({
        //TODO fix this
//        receiveMessages(queueUrlVal = "dev-mao-shipping-events)
        counter.incrementAndGet()
    }, 0, 100, TimeUnit.MILLISECONDS)
}


fun readFileContents(file: File): String
        = file.readText(Charsets.UTF_8)



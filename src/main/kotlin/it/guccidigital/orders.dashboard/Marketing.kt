package it.guccidigital.orders

import com.google.gson.Gson
import it.guccidigital.*
import it.guccidigital.models.Order
import kotlinx.coroutines.delay
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking

val appItDashboard: HttpHandler = routes(
    //register some routes
    "/orders/pricing" bind Method.GET to {
        println(message = " extract and show dashboard " )
        Response(OK).body("TO BE CONSTRUCTED")
    }
)


fun main() {
    val printingApp: HttpHandler = PrintRequest().then(appItDashboard)
    val server = printingApp.asServer(SunHttp(9002)).start()
    //start a loop over the queue
    runBlocking {
            receiveIt()
    }
    println("Server started on " + server.port())
}

suspend fun receiveIt() {
    val worker = Executors.newFixedThreadPool(5)

//    var submit = worker.submit{
//        Thread.sleep(100L)
//        receiveMessages(queueUrlVal = "dev-mao-order-events"))
//    }

    while (true){
        delay(10000L)
        val gson = Gson()
        //consume messages over the queue
        val extractedMessage: Order = gson.fromJson(receiveMessages(queueMarketingUrl).body, Order::class.java)
        println(" Order received = " + extractedMessage.item + " - " + extractedMessage.price)
        //based on a business logic, send to a subsequent queue
        sendPriceMovement(decidePriceChanges(extractedMessage))
    }
}

fun decidePriceChanges(extractedMessage: Order): Boolean {
//    TODO("Price should change it item has already been sell a lot")
    return when {
        extractedMessage.price>800 -> {
            println(" Lower/Raise price");
            true
        }
        else -> false
    }
}


suspend fun sendPriceMovement(decidePriceChanges: Boolean) {
//    TODO("If true then send to a specific queue with price direction ")
    if (decidePriceChanges) sendMessages(queueUrlVal = priceQueue, "Lower/Raise price")
}



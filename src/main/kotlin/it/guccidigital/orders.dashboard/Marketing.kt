package it.guccidigital.orders

import it.guccidigital.*
import it.guccidigital.models.Order
import kotlinx.coroutines.delay
import org.http4k.core.*
import java.util.concurrent.Executors
import software.amazon.awssdk.services.sqs.model.Message


suspend fun receiveMarketingAlerts() {
    val worker = Executors.newFixedThreadPool(5)
    while (true){
        delay(10000L)
        val messages = receiveSQSMessage(queueMarketingUrl.toString())
        println(" Marketing alerts to be managed " + messages.size)
        //consume messages over the queue
        //based on some business logic, send to a subsequent queue
        val pippo = if(4/2==2) "pluto" else "ciao"
        val pippo2: String = when(4/2) { 2 -> "pluto" else -> "ciao" }
        messages.map {
                singleMessage
                    ->   when(decidePriceChanges(getJsonOrder(singleMessage))) {
                            true -> {
                                runCatching {
                                        sendSQSMessage(queueUrlVal = priceQueue, "Lower/Raise price") }
                                    .onSuccess {
                                        deleteSQSMessage(queueMarketingUrl,singleMessage)
                                    }
                            }
                            false ->
                            {
                                println("no movement")
                                deleteSQSMessage(queueMarketingUrl,singleMessage)
                            }
                        }
        }
        println(" Marketing alerts has been managed ")
    }
}

private fun getJsonOrder(singleMessage: Message): Order? {
    println(" Json syntax -> " + singleMessage.body())
    return gson.fromJson(singleMessage.body(), Order::class.java)
}

@Deprecated("Use the new receiveMarketingAlerts method")
suspend fun receiveIt() {
    val worker = Executors.newFixedThreadPool(5)
    while (true){
        delay(10000L)
        //consume messages over the queue
        val extractedMessage: Order = gson.fromJson(receiveMessages(queueMarketingUrl).body, Order::class.java)
        println(" Order received = " + extractedMessage.item + " - " + extractedMessage.price)
        //based on a business logic, send to a subsequent queue
        decidePriceChanges(extractedMessage).apply { sendSQSMessage(queueUrlVal = priceQueue, "Lower/Raise price") }
    }
}

fun decidePriceChanges(extractedMessage: Order?): Boolean {
//    TODO("Price should change it item has already been sell a lot")
    return when {
        extractedMessage?.price!!.compareTo(800) > 0 -> {
            println(" Lower/Raise price");
            true
        }
        else -> {
            println(" No price movement");
            false
        }
    }
}


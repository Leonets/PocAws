package it.guccidigital.orders

import createBucketAws
import extractShippingDetailsAws
import getContentsAws
import it.guccidigital.*
import it.guccidigital.models.Order
import it.guccidigital.models.Orders
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SERVICE_UNAVAILABLE
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import putObjectStreamAws
import software.amazon.awssdk.services.sqs.model.Message
import java.io.File
import java.lang.Thread.currentThread

val ordersLens = Body.auto<Orders>().toLens()
val loopMarketingTiming = 60000L
val loopOrdersTiming = 20000L
val httpPort = 9000

fun main() {
    val printingApp: HttpHandler = PrintRequest().then(app)

    val server = printingApp.asServer(SunHttp(httpPort)).start()
    println("Server started on " + server.port())

    runBlocking {
        //create gucci bucket
        createBucketAws(bucketName)

        launch {
            //start a loop over the marketing queue
            managePriceAlerts()
        }
        launch {
            //start a loop to manage orders executions
            manageOrders()
        }
    }
}

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
            savePayload(it = it)
            //process means the single orders are sent to a queue and to a topic
            process(it = it)
        }
        Response(OK).body("Orders received")
    },

    "/orders/pricing" bind Method.GET to {
        println(message = " extract and show current pricing policy " )
        Response(OK).body("TO BE CONSTRUCTED")
    },
    "/orders/shipping" bind Method.GET to {
        println(message = " extract and show shipping countries " )

        //operation:
        //1. extract object from S3 store (list<S3Object>)
        //2. create an object Orders by using the gson library
        //3. to create the Orders objects uses the key (it.key) to get the contents of the object inside the S3 Vault
        //4. by using the ordersList property it gets back the property (List<Order>)
        //5. by using the flatmap function it aggregates all the elements from single element transformation
        val result1 = extractShippingDetailsAws(bucketName).contents()
            ?.flatMap  {gson.fromJson(getContentsAws(bucketName,it.key()), Orders::class.java).ordersList }

        result1?.forEach { println("Order $it") }

        //operation:
        //1. render country and price of the orders received
        Response(status = OK).body(result1?.joinToString { "Country = ${it.destinationCountry}  Price = ${it.price}  \n" } ?: "no data")
    },
    "/orders/dashboard" bind Method.GET to {
        println(message = " extract and show dashboard " )

        //operation, the same as shipping
        val result1 = extractShippingDetailsAws(bucketName).contents()
            ?.flatMap  {gson.fromJson(getContentsAws(bucketName,it.key()), Orders::class.java).ordersList }
        //get the sum of italian orders
//        val orderIt = result1?.filter { it.destinationCountry=="IT" }
//        val sum = orderIt?.sumOf { it.price }

        val output = result1?.groupingBy { it.destinationCountry }?.aggregate { key, accumulator: Int?, element, first
                -> if (first)
                        element.price
                    else
                        accumulator!!.plus(element.price)
        }

        //operation:
        //1. render total amount of orders per country
        Response(status = OK).body(output?.entries?.joinToString { it.key + " = " + it.value }?: "no data")
    }
)


suspend fun savePayload(it: Request) {
    // extract the body from the message
    val extractedMessage: Orders = ordersLens(it)
    //sending data to the bucket
    val dataToSent: String = gson.toJson(extractedMessage)
    //put the payload in the bucket with a random key (json sintax)
    putObjectStreamAws(bucketName, randomStringByJavaRandom(), contents = dataToSent)
}

fun process(it: Request) {
    // extract the body from the message
    val extractedMessage: Orders = ordersLens(it)
    //loop around and execute
    extractedMessage.ordersList.forEach { it ->
        //send to a SQS queue (marketing)
        sendSQSMessage(queueMarketingUrl, gson.toJson(it))
        //send to a SNS topic (orders)
        pubSNSTopic(message = it.toString(), topicArn = order_topic_arn)
    }
}


suspend fun manageOrders() {
    coroutineScope {
        while (true) {
            try {
                //consume messages over the queues
                manageQueue(queueAccountingUrl, "Accounting")

                launch {//I dont' want to wait here
                    manageQueue(queueOrderUrl, "Order")
                }

                manageQueue(queueShippingUrl, "Shipping")
                delay(loopOrdersTiming) //non blocking
            } catch (ex: Exception) {
                println("${currentThread().name} failed with {$ex}. Retrying...")
            }
            println(" >>> Some orders have been managed \n")
        }
    }
}

private fun manageQueue(queueName: String, subsystem: String) {
    val messages = receiveSQSMessage(queueName)
    println("  message from $queueName to be managed " + messages.size)
    //ask for execution to a subsytem
    messages.map { singleMessage
        -> when(execute(singleMessage, subsystem) == Status.OK) {
            true -> {
                deleteSQSMessage(queueName, singleMessage)
            }
            false -> {
            println(" subsystem unavailable $subsystem" )
            }
        }
    }
}

fun execute(it: Message, subsystem: String): Status {
    var response: Response = Response(SERVICE_UNAVAILABLE)
//    GlobalScope.launch {
        //if I add a coroutine it executes the request but it does not wait!!
        val client: HttpHandler = JavaHttpClient()
        val printingClient: HttpHandler = DebuggingFilters.PrintResponse().then(client)
        val request = Request(GET, Uri.of("http://localhost:9001/${subsystem}"), it.body())
        //executes the request
        response = printingClient(request)
        println(" output from ${subsystem} " + response.bodyString())
//    }
    return response.status
}

suspend fun managePriceAlerts() {
    while (true){
        try {
            delay(loopMarketingTiming) //non blocking
            //consume messages over the queue
            val messages = receiveSQSMessage(queueMarketingUrl.toString())
            println(" Marketing/Price alerts to be managed " + messages.size)
            //based on some business logic, send to a subsequent queue
            messages.map {
                    singleMessage
                    //decide if price needs to be increased
                    ->   when(decidePriceChanges(getJsonOrder(singleMessage))) {
                    true -> {
                        runCatching {
                            //logic has decided to
                            sendSQSMessage(queueUrlVal = priceQueue, "Raise price over item " + getJsonOrder(singleMessage)?.item) }
                            .onSuccess {
                                //if the message has been sent over, then delete for avoid any subsequent read
                                deleteSQSMessage(queueMarketingUrl,singleMessage)
                            }
                    }
                    false ->
                    {
                        println("no price movement")
                        //when the price does not increase then delete the message
                        deleteSQSMessage(queueMarketingUrl,singleMessage)
                    }
                }
            }
        } catch (ex: Exception) {
            println("${currentThread().name} failed with {$ex}. Retrying...")
        }
        println(" Marketing/Price alerts has been managed \n")
    }
}

private fun getJsonOrder(singleMessage: Message): Order? {
    println(" Json syntax -> " + singleMessage.body())
    return gson.fromJson(singleMessage.body(), Order::class.java)
}

fun decidePriceChanges(extractedMessage: Order?): Boolean {
//    TODO("Price should change it item has already been sell a lot")
    val currentMoment = Clock.System.now()
    return when {
        //bizzarre logic: price over 800eur will raise during hours between 15 and 21
        (extractedMessage?.price!!.compareTo(800) > 0)
                && 13 < currentMoment.toLocalDateTime(TimeZone.UTC).time.hour
                && currentMoment.toLocalDateTime(TimeZone.UTC).time.hour < 21
        -> {
            println(" Raise price");
            true
        }
        else -> {
            println(" No price movement");
            false
        }
    }
}

fun readFileContents(file: File): String
        = file.readText(Charsets.UTF_8)

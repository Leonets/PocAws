package it.guccidigital.orders

import createBucketAws
import extractShippingDetailsAws
import getContentsAws
import getObject
import it.guccidigital.*
import it.guccidigital.models.Order
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
import putObjectStreamAws
import software.amazon.awssdk.services.s3.model.S3Object
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

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
            process(it = it)
        }
        Response(OK).body("Orders received")
    },

    "/orders/pricing" bind Method.GET to {
        println(message = " extract and show dashboard " )
        Response(OK).body("TO BE CONSTRUCTED")
    },
    "/orders/shipping" bind Method.GET to {
        println(message = " extract and show dashboard " )

//        val result = runBlocking {
//            //extract objects from s3
//            extractShippingDetailsAws(bucketName).contents().forEach {it: S3Object? ->
//
//                it?.key()?.let { it1 -> getObject(bucketName, it1,"temp."+it.key()) }
//                val content: String = readFileContents(File("temp."+it?.key()))
//                println(" read again contents from saved file   = " + content)
//                //for each object extract the destination country
//                gson.fromJson(content , Orders::class.java).value.forEach { it: Order ->
//                    println(" destination country = " + it.destinationCountry)
//                }
//            }
//        }

        val countriesList: MutableList<String>  = ArrayList<String>();
        //refactored with FP
        val result1 = extractShippingDetailsAws(bucketName).contents()
            ?.flatMap  {gson.fromJson(getContentsAws(bucketName,it.key()), Orders::class.java).value }

        result1?.forEach { println("Order $it") }

//        val countriesList: MutableList<String>  = ArrayList<String>();
//        //refactored with FP
//        val result1 = runBlocking {
//            extractShippingDetails(bucketName).contents?.map {
//                it.key?.let {
//                        it1 ->
//                            getObject(bucketName, it1,"temp."+it)
//                }
//                println(" found payload  = " + it.key)
//                gson.fromJson(
//                    readFileContents(File("temp."+it.key)) , Orders::class.java).value.map {
//                    println(" destination country FP = " + it.destinationCountry)
//                }
//            }
//        }

        //refactored with a side effect on the list to send to web page
//        val result2 = runBlocking {
//            extractShippingDetails(bucketName).contents?.forEach {
//                gson.fromJson(readFileContents(File("temp."+it.key)) , Orders::class.java).value
//                    .forEach { countriesList.add(it.destinationCountry) }
//            }
//        }
//        Response(OK).body(countriesList.joinToString("\n"))

        Response(OK).body("TO BE CONSTRUCTED")
    }
)


fun main() {
    val printingApp: HttpHandler = PrintRequest().then(app)

    val server = printingApp.asServer(SunHttp(9000)).start()
    println("Server started on " + server.port())

    println("Create bucket ")
    //create gucci bucket
    runBlocking {
        createBucketAws(bucketName)
        //start a loop over the marketing queue
        receiveMarketingAlerts()
        //perchè se questa function sopra viene eseguita prima dello start del server
        //allora lo start non viene mai eseguito (eppure dovrebbe essere suspend)
        receiveForShipping()
    }
}

suspend fun savePayload(it: Request) {
    //TODO duplicate code
    val ordersLens = Body.auto<Orders>().toLens()
    // extract the body from the message
    val extractedMessage: Orders = ordersLens(it)
    //sending data to the bucket
    val dataToSent: String = gson.toJson(extractedMessage)
    (" sending data to the bucket " + dataToSent)
    //put the payload in the bucket with a random key (json sintax)
    putObjectStreamAws(bucketName, randomStringByJavaRandom(), contents = dataToSent)
    println(" object saved to bucket " )
}

suspend fun process(it: Request) {
    val ordersLens = Body.auto<Orders>().toLens()
    // extract the body from the message
    val extractedMessage: Orders = ordersLens(it)

    extractedMessage.value.forEach { it ->
        //send to a SQS queue
        sendSQSMessage(queueMarketingUrl, gson.toJson(it))
        //send to a SNS topic
        pubSNSTopic(message = it.toString(), topicArn = topicArnVal)
    }
}


fun receiveForShipping() {
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

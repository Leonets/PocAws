package it.guccidigital

import aws.smithy.kotlin.runtime.http.Url
import com.google.gson.Gson
import java.net.URI
import java.util.concurrent.ThreadLocalRandom
import kotlin.streams.asSequence


val gson = Gson()

val bucketName by lazy { "guccibucket" }

val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')


fun randomStringByJavaRandom() = ThreadLocalRandom.current()
    .ints(10L, 0, charPool.size)
    .asSequence()
    .map(charPool::get)
    .joinToString("")


//constant for queues and topic
const val queueMarketingUrl: String = "http://localhost:9324/queue/mao-marketing-events"
const val queueAccountingUrl: String = "http://localhost:9324/queue/dev-mao-accounting-events"
const val queueOrderUrl: String = "http://localhost:9324/queue/dev-mao-order-events"
const val queueShippingUrl: String = "http://localhost:9324/queue/dev-mao-shipping-events"

val order_topic_arn: String = "arn:aws:sns:elasticmq-2:123450000001:local-orders_topic"
val priceQueue: String = "http://localhost:9324/queue/mao-pricing-policy-events"

val sqsEndpointUrl = Url.parse("http://localhost:9324")
val s3EndpointUrl: Url = Url.parse("http://localhost:9090")
val topicEndpointUrl = Url.parse("http://localhost:9911")

val endpointURI = URI.create("http://localhost:9324")
val topicEndpointURI = URI.create("http://localhost:9911")
val s3EndpointURI: URI = URI.create("http://localhost:9090")


//fun <T> singletonList(item: T): List<T> {
//    // ...
//    return listOf(item)
//}



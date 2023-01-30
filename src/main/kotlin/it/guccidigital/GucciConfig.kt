package it.guccidigital

import aws.smithy.kotlin.runtime.http.Url
import com.google.gson.Gson
import java.net.URI
import java.util.concurrent.ThreadLocalRandom
import kotlin.streams.asSequence

public class Config(val bucketName: String)

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
val topicArnVal: String = "arn:aws:sns:elasticmq-2:123450000001:local-orders_topic"
val priceQueue: String = "http://localhost:9324/queue/mao-pricing-policy-events"

val endpointUrl = Url.parse("http://localhost:9324")
val s3EndpointUrl: Url = Url.parse("http://localhost:9090");

val endpointURI = URI.create("http://localhost:9324")
val topicEndpointURI = URI.create("http://localhost:9911")
val s3EndpointURI: URI = URI.create("http://localhost:9090");


fun <T> singletonList(item: T): List<T> {
    // ...
    return listOf(item)
}

//fun <T> T.basicToString(): String { // extension function
//    // ...
//}



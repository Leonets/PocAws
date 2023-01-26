package it.guccidigital

import java.util.concurrent.ThreadLocalRandom
import kotlin.streams.asSequence

public class Config(val bucketName: String)

val bucketName by lazy { "GucciDemoBucket" }
val key by lazy { "unknown" }
const val objectPath = "/home/lbattagli@florence-consulting.it/Projects/GucciDemo1/storage/orders.json"
val savePath = "/home/lbattagli@florence-consulting.it/Projects/GucciDemo1/storage/orders.json"
    get() = field
val toBucket = "/home/lbattagli@florence-consulting.it/Projects/GucciDemo1/storage/orders.json"

val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')


fun randomStringByJavaRandom() = ThreadLocalRandom.current()
    .ints(10L, 0, charPool.size)
    .asSequence()
    .map(charPool::get)
    .joinToString("")


//constant for queues and topic
val queueMarketingUrl: String = "http://localhost:9324/queue/mao-marketing-events"
val topicArnVal: String = "arn:aws:sns:elasticmq-2:123450000001:local-orders_topic"
val priceQueue: String = "http://localhost:9324/queue/mao-pricing-policy-events"

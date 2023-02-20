package it.guccidigital.orders

import aws.smithy.kotlin.runtime.http.Url
import com.google.gson.Gson
import it.guccidigital.models.Orders
import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import java.net.URI
import java.util.concurrent.ThreadLocalRandom
import kotlin.streams.asSequence

val mockHttpPort = 9001

//fun <T> singletonList(item: T): List<T> {
//    // ...
//    return listOf(item)
//}



package it.guccidigital.orders

import it.guccidigital.gson
import it.guccidigital.httpPort
import it.guccidigital.models.Salesposting
import it.guccidigital.sendSQSMessage
import org.http4k.core.HttpHandler
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.io.File

const val salespostingUrl: String = "http://localhost:9324/queue/dev-mao-salesposting-events"

fun main() {
    send()
}


    fun send() {
        val sampleSalesposting = readFileContents(File("src/test/resources/it/guccidigital/test/01_1_Item_More_Reasons.json"))
        val singleMessage = Salesposting("id1", "M", 1440, "blue", "Via Roma, 2", "50144", "IT")
        sendSQSMessage(salespostingUrl,sampleSalesposting)
    }


    fun getJsonOrder(singleMessage: String): Salesposting? {
        return gson.fromJson(singleMessage, Salesposting::class.java)
    }

    fun readFileContents(file: File): String
            = file.readText(Charsets.UTF_8)

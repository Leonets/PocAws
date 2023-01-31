package it.guccidigital.playground

import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.ListTopicsRequest
import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.ListQueuesRequest
import it.guccidigital.orders.app
import it.guccidigital.topicEndpointUrl
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrdersTest {

    @Test
    suspend fun `Ping test`() {
        assertEquals(app(Request(GET, "/ping")), Response(OK).body("pong"))

        //list queues and topic
        listSNSTopics()
        listQueues()
    }


    suspend fun listQueues() {
        println("\nList Queues")

        val prefix = "dev"
        //prefix for filtering queue names
        val listQueuesRequest = ListQueuesRequest {
            queueNamePrefix = prefix
        }

        SqsClient {endpointUrl = endpointUrl;  region = "elasticmq" }.use { sqsClient ->
            //list queues
            val response = sqsClient.listQueues(listQueuesRequest)
            //if queues are present, print their urls
            response.queueUrls?.forEach { url ->
                println(url)
            }
        }
    }

    suspend fun listSNSTopics() {
        SnsClient { endpointUrl = topicEndpointUrl ;  region = "elasticmq" }.use { snsClient ->
            val response = snsClient.listTopics(ListTopicsRequest { })
            response.topics?.forEach { topic ->
                println("The topic ARN is ${topic.topicArn}")
            }
        }
    }

}

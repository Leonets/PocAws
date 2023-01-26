package it.guccidigital

// snippet-sourcedescription:[SendMessages.kt demonstrates how to send a message to an Amazon Simple Queue Service (Amazon SQS) queue.]
// snippet-keyword:[AWS SDK for Kotlin]
// snippet-service:[Amazon Simple Queue Service]
/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

// snippet-start:[sqs.kotlin.send_messages.import]
import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.ListQueuesRequest
import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.ListTopicsRequest
import aws.sdk.kotlin.services.sqs.model.ReceiveMessageRequest
import aws.smithy.kotlin.runtime.http.Url

// snippet-end:[sqs.kotlin.send_messages.import]

val topicEndpointUrl = Url.parse("http://localhost:9911")
val queueEndpointUrl = Url.parse("http://localhost:9324")

/**
Before running this Kotlin code example, set up your development environment,
including your credentials.

For more information, see the following documentation topic:
https://docs.aws.amazon.com/sdk-for-kotlin/latest/developer-guide/setup.html
 */
suspend fun main(args: Array<String>) {

    //list queues and topic
    listSNSTopics()
    listQueues()

    println("The multi AWS SQS operation example is complete!")
}

suspend fun listQueues() {
    println("\nList Queues")

    val prefix = "dev"
    //prefix for filtering queue names
    val listQueuesRequest = ListQueuesRequest {
        queueNamePrefix = prefix
    }

    SqsClient {endpointUrl = queueEndpointUrl;  region = "elasticmq" }.use { sqsClient ->
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
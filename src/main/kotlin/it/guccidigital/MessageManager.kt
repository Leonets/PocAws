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
import aws.sdk.kotlin.services.sqs.model.SendMessageRequest
import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.PublishRequest
import aws.sdk.kotlin.services.sns.publish
import aws.sdk.kotlin.services.sqs.model.Message
import aws.sdk.kotlin.services.sqs.model.ReceiveMessageRequest
import aws.smithy.kotlin.runtime.http.Url



@Deprecated("Use the new sendSQSMessage method")
suspend fun sendMessages(queueUrlVal: String, message: String) {
    println("\nSend message to " + queueUrlVal)
    val sendRequest = SendMessageRequest {
        queueUrl = queueUrlVal
        messageBody = message
        delaySeconds = 10
    }

    SqsClient {endpointUrl = Url.parse("http://localhost:9324") ; region = "elasticmq" }.use { sqsClient ->
        sqsClient.sendMessage(sendRequest)
        println("A single message was successfully sent.")
    }
}


suspend fun pubTopic(topicArnVal: String, messageVal: String) {

    println("\n Ready to send to topic !! " + topicArnVal + " the value " + messageVal)
    val request = PublishRequest {
        val message = messageVal
        val topicArn = topicArnVal
    }

    val snsClient = SnsClient { endpointUrl = Url.parse("http://localhost:9911") ;  region = "elasticmq" }
    snsClient.publish { request }
    println(" message sent.")

//    SnsClient { endpointUrl = Url.parse("http://localhost:9911") ; region = "elasticmq" }.use { snsClient ->
//        val result = snsClient.publish(request)
//        println("${result.messageId} message sent.")
//    }
}



@Deprecated("Use the new receiveSQSMessage method")
suspend fun receiveMessages(queueUrlVal: String?): Message {

    println("Retrieving messages from $queueUrlVal")

    val receiveMessageRequest = ReceiveMessageRequest {
        queueUrl = queueUrlVal
        maxNumberOfMessages = 5
        waitTimeSeconds = 20 //enabled long polling for receiving the SQS messages by setting the wait time as 20 seconds ?
    }

    SqsClient {endpointUrl = queueEndpointUrl; region = "elasticmq" }.use { sqsClient ->
        val response = sqsClient.receiveMessage(receiveMessageRequest)
        response.messages?.forEach { message ->
            println(" Message has been read from queue = " + message.body)
            return message
        }
    }
    return TODO("Provide the return value")
}
// snippet-end:[sqs.kotlin.get_messages.main]

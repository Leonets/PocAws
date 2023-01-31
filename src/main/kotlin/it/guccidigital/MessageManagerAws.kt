package it.guccidigital

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

val sqsClient: SqsClient = SqsClient.builder()
    .region(Region.of("elasticmq"))
    .credentialsProvider(ProfileCredentialsProvider.create())
    .endpointOverride(endpointURI)
    .build()

val snsClient: SnsClient = SnsClient.builder()
    .region(Region.of("elasticmq"))
    .credentialsProvider(ProfileCredentialsProvider.create())
    .endpointOverride(topicEndpointURI)
    .build()

fun sendSQSMessage(queueUrlVal: String, message: String) {
    val sendRequest = SendMessageRequest.builder().messageBody(message).queueUrl(queueUrlVal).build()
    sqsClient.sendMessage(sendRequest)
    println("A single message was successfully sent to " + queueUrlVal)
}

fun receiveSQSMessage(queueUrlVal: String): List<Message> {
    val receiveRequest = ReceiveMessageRequest.builder()
            .maxNumberOfMessages(5)
            .waitTimeSeconds(5)
            .queueUrl(queueUrlVal).build()
    return sqsClient.receiveMessage(receiveRequest).messages()
    println("Some message have been read from " + queueUrlVal)
}

fun deleteSQSMessage(queueUrlVal: String, message: Message) {
    try {
        val deleteRequest = DeleteMessageRequest.builder()
            .receiptHandle(message.receiptHandle())
            .queueUrl(queueUrlVal)
            .build()
        val deleteMessage = sqsClient.deleteMessage(deleteRequest)
        println("Some message have been deleted from " + queueUrlVal)
    } catch (ex: Exception) {
        println("${Thread.currentThread().name} failed with {$ex}. Retrying...")
    }
}

fun pubSNSTopic(message: String?, topicArn: String?) {
    val request: PublishRequest = PublishRequest.builder()
            .message(message)
            .topicArn(topicArn)
            .build()
    val result: PublishResponse = snsClient.publish(request)
    println(result.messageId() + " Message sent to topic. Status is " + result.sdkHttpResponse().statusCode())
}
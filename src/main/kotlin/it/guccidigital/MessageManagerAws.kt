package it.guccidigital

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

var sqsClient: SqsClient = SqsClient.builder()
    .region(Region.of("elasticmq"))
    .credentialsProvider(ProfileCredentialsProvider.create())
    .endpointOverride(endpointURI)
    .build()

var snsClient: SnsClient = SnsClient.builder()
    .region(Region.of("elasticmq"))
    .credentialsProvider(ProfileCredentialsProvider.create())
    .endpointOverride(topicEndpointURI)
    .build()

fun sendSQSMessage(queueUrlVal: String, message: String) {
    println("\nSend message to " + queueUrlVal)
    val sendRequest = SendMessageRequest.builder().messageBody(message).queueUrl(queueUrlVal).build()
    sqsClient.sendMessage(sendRequest)
    println("A single message was successfully sent.")
}

fun receiveSQSMessage(queueUrlVal: String): List<Message> {
    println("\nReceive message from " + queueUrlVal)
    val receiveRequest = ReceiveMessageRequest.builder()
            .maxNumberOfMessages(5)
            .queueUrl(queueUrlVal).build()
    return sqsClient.receiveMessage(receiveRequest).messages()
    println("Some message have been read ")
}

fun deleteSQSMessage(queueUrlVal: String, message: Message) {
    println("\nDelete message from " + queueUrlVal)
    val deleteRequest = DeleteMessageRequest.builder()
        .receiptHandle(message.receiptHandle())
        .queueUrl(queueUrlVal)
        .build()
    sqsClient.deleteMessage(deleteRequest)
    println("Some message have been deleted ")
}

fun pubSNSTopic(message: String?, topicArn: String?) {
    val request: PublishRequest = PublishRequest.builder()
            .message(message)
            .topicArn(topicArn)
            .build()
    val result: PublishResponse = snsClient.publish(request)
    println(result.messageId() + " Message sent to topic. Status is " + result.sdkHttpResponse().statusCode())
}
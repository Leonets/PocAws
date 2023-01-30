@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

import it.guccidigital.s3EndpointURI
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.util.*


var s3Client: S3Client = S3Client.builder()
    .region(Region.of("elasticmq"))
    .credentialsProvider(ProfileCredentialsProvider.create())
    .endpointOverride(s3EndpointURI)
    .build()

suspend fun main(args: Array<String>) {

    val usage = """
    Usage:
        <bucketName> <key> <objectPath> <savePath> <toBucket>

    Where:
        bucketName - The Amazon S3 bucket to create.
        key - The key to use.
        objectPath - The path where the file is located (for example, C:/AWS/book2.pdf).   
        savePath - The path where the file is saved after it's downloaded (for example, C:/AWS/book2.pdf).     
        toBucket - An Amazon S3 bucket to where an object is copied to (for example, C:/AWS/book2.pdf). 
        """

    val bucketName = "guccibucket"
    val key = "key"
    val objectPath = "/tmp/abc.txt"
    val savePath = "/tmp/save.txt"
    val toBucket = "/tmp/to.txt"
    println(bucketName + " - " + key)
    println(objectPath + " - " + savePath + " - " + toBucket)

    // Create an Amazon S3 bucket.
//    createBucketAws(bucketName)
    println(" bucket created ")

    //List
    listBucketsAws()
    println(" buckets listed ")

    // Update a local file to the Amazon S3 bucket.
    putObjectStreamAws(bucketName, key, "prova")
    println(" key put inside bucket ")

    // List objects.
    listObjectsAws(bucketName).contents().map {
        getContentsAws(bucketName, it.key())
    }
    println(" list objects inside bucket ")

    // Download the object to another local file.
    val foundsomething = getContentsAws(bucketName, keyName = key)
    println(" object get from bucket " + foundsomething)

    val listObjects = extractShippingDetailsAws(bucketName)
    listObjects.contents().map { println(" object " + it.key()) }
    println(" extractShippingDetailsAws ")

    println("All Amazon S3 operations were successfully performed")
}

fun createBucketAws(bucketName: String) {

    val s3Waiter = s3Client.waiter();
    val bucketRequest = CreateBucketRequest.builder()
        .bucket(bucketName)
        .build();

    s3Client.createBucket(bucketRequest);
    val bucketRequestWait = HeadBucketRequest.builder()
        .bucket(bucketName)
        .build();

    // Wait until the bucket is created and print out the response.
    val waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
    waiterResponse.matched().response().ifPresent(System.out::println);
    println(bucketName +" is ready");
}

fun listBucketsAws() {
    // List buckets
    val listBucketsRequest = ListBucketsRequest.builder().build()
    val listBucketsResponse: ListBucketsResponse = s3Client.listBuckets(listBucketsRequest)
    listBucketsResponse.buckets().stream().forEach { x -> println(x.name()) }
}



fun getContentsAws(bucketName: String, keyName: String): String? {

    val getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(keyName)
        .build()
    val response = s3Client.getObject(getObjectRequest)

    return String(response.readAllBytes())
}

fun putObjectStreamAws(bucketName: String, objectKey: String, contents: String) {

    val objectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(objectKey)
        .build()

    s3Client.putObject(objectRequest, RequestBody.fromString(contents))
//    s3Client.putObject(objectRequest, RequestBody.fromByteBuffer(getRandomByteBuffer(10_000)));
}

@Throws(IOException::class)
private fun getRandomByteBuffer(size: Int): ByteBuffer? {
    val b = ByteArray(size)
    Random().nextBytes(b)
    return ByteBuffer.wrap(b)
}

fun listObjectsAws(bucketName: String): ListObjectsResponse {
    // List objects
    val listObjects = ListObjectsRequest
        .builder()
        .bucket(bucketName)
        .build()

    val res: ListObjectsResponse = s3Client.listObjects(listObjects)
    return res
}

fun extractShippingDetailsAws(bucketName: String): ListObjectsResponse {

    val request = ListObjectsRequest.builder().bucket(bucketName).build()

    println("\n lookup objects inside " + bucketName)
    return s3Client.listObjects(request)
}



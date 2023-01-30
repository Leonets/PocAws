import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.*
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.writeToFile
import aws.smithy.kotlin.runtime.http.Url
import it.guccidigital.s3EndpointUrl
import java.io.ByteArrayInputStream
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import kotlin.system.exitProcess

val key = "unknown2"
val objectPath = "/home/lbattagli@florence-consulting.it/Projects/GucciDemo1/storage/orders.json"
val savePath = "/home/lbattagli@florence-consulting.it/Projects/GucciDemo1/storage/orders-saved.json"
val toBucket = "/home/lbattagli@florence-consulting.it/Projects/GucciDemo1/storage/orders.json"

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
    println(" args size = " + args.size)
    if (args.size != 5) {
        println(usage)
        exitProcess(1)
    }

    val bucketName = args[0]
    val key = args[1]
    val objectPath = args[2]
    val savePath = args[3]
    val toBucket = args[4]
    println(bucketName + " - " + key)
    println(objectPath + " - " + savePath + " - " + toBucket)

    // Create an Amazon S3 bucket.
    createBucket(bucketName)
    println(" bucket created ")

    // Update a local file to the Amazon S3 bucket.
    putObject(bucketName, key, objectPath)
    println(" key put inside bucket ")

    // Download the object to another local file.
    getObject(bucketName, key, savePath)
    println(" object get from bucket ")

    // List all objects located in the Amazon S3 bucket.
    listBucketObs(bucketName)
    println(" objects listed from created ")

    listBucketObs("GucciDemoBucket")
    println(" objects listed from bucket Gucci ")

    //get contents of a key
    val content = getContents(bucketName, key)
    println(" bucket contents by key  " + content)

    // Copy the object to another Amazon S3 bucket
//    copyBucketOb(bucketName, key, toBucket)
//
    // Delete the object from the Amazon S3 bucket.
    deleteBucketObs(bucketName, key)
//
    // Delete the Amazon S3 bucket.
    deleteBucket(bucketName)

    // Delete the GucciDemoBucket
    val gucciBucket = "GucciDemoBucket"
    deleteBucketObs(gucciBucket, "fMam2J4Ud7")
    deleteBucket(gucciBucket)

    println("All Amazon S3 operations were successfully performed")
}

suspend fun createBucket(bucketName: String) {

    val request = CreateBucketRequest {
        bucket = bucketName
    }

    S3Client {endpointUrl = s3EndpointUrl;  region = "it-s3" }.use { s3 ->
        s3.createBucket(request)
        println("$bucketName is ready")
    }
}

suspend fun putObject(bucketName: String, objectKey: String, objectPath: String) {

    val metadataVal = mutableMapOf<String, String>()
    metadataVal["myVal"] = "test"

    val request = PutObjectRequest {
        bucket = bucketName
        key = objectKey
        metadata = metadataVal
        this.body = Paths.get(objectPath).asByteStream()
    }

    S3Client {endpointUrl = s3EndpointUrl; region = "it-s3" }.use { s3 ->
        val response = s3.putObject(request)
        println("Tag information is ${response.eTag}")
    }
}

suspend fun putObjectStream(bucketName: String, objectKey: String, objectPath: ByteStream) {

    val metadataVal = mutableMapOf<String, String>()
    metadataVal["myVal"] = "test"

    val request = PutObjectRequest {
        bucket = bucketName
        key = objectKey
        metadata = metadataVal
        this.body = objectPath
    }

    S3Client {endpointUrl = s3EndpointUrl; region = "it-s3" }.use { s3 ->
        val response = s3.putObject(request)
        println("\n Stream Tag information is ${response.eTag}")
    }
}


suspend fun getObject(bucketName: String, keyName: String, path: String) {

    val request = GetObjectRequest {
        key = keyName
        bucket = bucketName
    }

    S3Client {endpointUrl = s3EndpointUrl; region = "it-s3" }.use { s3 ->
        s3.getObject(request) { resp ->
            val myFile = File(path)
            resp.body?.writeToFile(myFile)
            println("\n Successfully read $keyName from $bucketName and saved to $path")
        }
    }
}

suspend fun getContents(bucketName: String, keyName: String): String? {

    val request = GetObjectRequest {
        key = keyName
        bucket = bucketName
    }

    S3Client {endpointUrl = s3EndpointUrl; region = "it-s3" }.use { s3 ->
        val value = s3.getObject(request) { resp ->
            println(" body inside s3 = " + resp.body?.toString())
            resp.body?.toString()
        }
        return value
    }
}

suspend fun listBucketObs(bucketName: String) {

    val request = ListObjectsRequest {
        bucket = bucketName
    }
    println("\n lookup objects inside " + bucketName)
    S3Client {endpointUrl = s3EndpointUrl; region = "it-s3" }.use { s3 ->

        val response = s3.listObjects(request)
        response.contents?.forEach { myObject ->
            println("The name of the key is ${myObject.key}")
            println("The owner is ${myObject.owner}")
            println("Data is  ${myObject.toString()}")
        }
    }
}

suspend fun extractShippingDetails(bucketName: String): ListObjectsResponse {

    val request = ListObjectsRequest {
        bucket = bucketName
    }
    println("\n lookup objects inside " + bucketName)
    S3Client {endpointUrl = s3EndpointUrl; region = "it-s3" }.use { s3 ->
        return s3.listObjects(request)
    }
}

suspend fun copyBucketOb(fromBucket: String, objectKey: String, toBucket: String) {

    var encodedUrl = ""
    try {
        encodedUrl = URLEncoder.encode("$fromBucket/$objectKey", StandardCharsets.UTF_8.toString())
    } catch (e: UnsupportedEncodingException) {
        println("URL could not be encoded: " + e.message)
    }

    val request = CopyObjectRequest {
        copySource = encodedUrl
        bucket = toBucket
        key = objectKey
    }
    S3Client {endpointUrl = s3EndpointUrl; region = "it-s3" }.use { s3 ->
        s3.copyObject(request)
    }
}

suspend fun deleteBucketObs(bucketName: String, objectName: String) {

    val objectId = ObjectIdentifier {
        key = objectName
    }

    val delOb = Delete {
        objects = listOf(objectId)
    }

    val request = DeleteObjectsRequest {
        bucket = bucketName
        delete = delOb
    }

    S3Client {endpointUrl = s3EndpointUrl; region = "it-s3" }.use { s3 ->
        s3.deleteObjects(request)
        println("$objectName was deleted from $bucketName")
    }
}

suspend fun deleteBucket(bucketName: String?) {

    val request = DeleteBucketRequest {
        bucket = bucketName
    }
    S3Client {endpointUrl = s3EndpointUrl; region = "it-s3" }.use { s3 ->
        s3.deleteBucket(request)
        println("The $bucketName was successfully deleted!")
    }
}


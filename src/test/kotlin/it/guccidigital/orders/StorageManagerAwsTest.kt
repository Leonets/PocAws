@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

import it.guccidigital.s3EndpointURI
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*



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


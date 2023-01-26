## Gucci Playground (experiment with Aws, Kotlin & Local Development)

# Coding core

Design and implement a system that is continuously pulling all the submitted orders 
to the network and detects different order's type for then submitting to the appropriate system.

* When an order is detected, store the information regarding the order, the total amount and the destination, 
       and then let a subsystem decide if the price needs to be changed 
* Given a list of countries, just filter orders involving (sending or receiving) those countries.
* Data can be stored in memory 
* Orders only need to be tracked since the application is started

## Notification Layer
An SNS service listens on the topic and republish on three different queue (Shipping; Accounting; Order)
## Queue Layer
An SQS service manages 5 different queues (Shipping; Accounting; Order, Marketing and Pricing_Policy)
## Data Layer
An S3 service offers the storage service

## Orders API
Build the following API/Topic/Queue that contains the following endpoints:

| Endpoint          | Medhod | Description                                                                          |
|-------------------|--------|--------------------------------------------------------------------------------------|
| /orders           | POST   | Receives orders in json format and delivers in queue 'Marketing' and topic 'Orders'  | 
|                   |        | (it also save the payload in the S3 GucciBucket)                                     |
| /orders/dashboard | GET    | Returns orders summary by showing country, total orders, total amount                |

## Marketing API
This service listens on its specific queue (Marketing) and then decides if the item price need to be changed 
and if then sends a message to a specific queue (Pricing_Policy)

| Endpoint        | Medhod | Description                         |
|-----------------|--------|-------------------------------------|
| /orders/pricing | GET    | Returns item price change direction |

## Shipping API
This service listens on its specific queue (Shipping) and then uses some subsystem for its execution.

| Endpoint         | Medhod | Description                                                        |
|------------------|--------|--------------------------------------------------------------------|
| /orders/shipping | GET    | Returns order shipping details looping over the S3 GucciBucket key |

## Operazioni eseguite

- publish su topic (a volte va a volte no, da aws cli adesso funziona)
- send/consumo su queue
- creazione bucket/inserimento key/lettura key e suo contenuto
- routes get e post
- http client verso api get/post
- data type conversion to/from json/httpmessage/message

## Errors
1)
c.a.t.s.util.S3MockExceptionHandler      : Responding with status 404: The specified bucket does not exist.
2)
Ready to send to topic !! arn:aws:sns:elasticmq-2:123450000001:local-orders_topic the value test from SendMessage.kt
Exception in thread "main" aws.sdk.kotlin.services.sns.model.SnsException: Failed to parse response as 'awsQuery' error
at aws.sdk.kotlin.services.sns.transform.PublishOperationDeserializerKt.throwPublishError(PublishOperationDeserializer.kt:56)
3)
gestione eccezioni, se pubTopic fallisce allora il loop si interrompe (Orders.kt)
4)
creazione di un secondo costruttore in un data class
5)
inline function
6)
ScheduledExecutor per eseguire il Long Polling (shipping.kt)
7)
come estrarre il risultato di un'elaborazione di un runblocking
8)
creazione di una classe per il contenimento di constant
9)
java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path $
10)
il TODO fa stop del processo ?
Exception in thread "main" kotlin.NotImplementedError: An operation is not implemented: Price should change it item has already been sell a lot
11)
introduce map , flatmap and filters

### Payload to add a list of addresses to monitor
```
{
“Orders”: 
    "Order":{
        "item": "id1",
        "size": "M"
        "price": 400,
        "color": "blue",
        "address": "via roma 1",
        "cap": "50144",
        "destinationCountry": "IT"
    },
    "Order":{
        "item": "id2",
        "size": "L"
        "price": 800,
        "color": "red",
        "address": "via milano 1",
        "cap": "50100",
        "destinationCountry": "IT"
    },
    "Order":{
        "item": "id3",
        "size": "L"
        "price": 3000,
        "color": "black",
        "address": "unknown 898",
        "cap": "19090",
        "destinationCountry": "JP"
    }
}
```

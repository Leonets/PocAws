# Gucci Playground (experiment with Aws, Kotlin & Local Development)

## Coding core

Design and implement a system that is continuously pulling all the submitted orders 
to the network and detects different order's type for then submitting to the appropriate system.

- When an order is detected, store the information regarding the order, the total amount and the destination, and then:
    - let a subsystem decide if the price needs to be changed
    - distribute the order to the subsequent systems (order, shipping and accounting) in pairs
                           with interacting with external system (API)
- [OPT] Given a list of countries, just filter orders involving those countries.
- [OPT] Create a dashboard showing grouped orders by country and total amount
- Data can be stored in memory 
- Orders only need to be tracked since the application is started

Please detail a solution using Kotlin and AWS (SQS, SNS, S3)

Please provide useful examples for evaluating the following:

- fault tolerance to sqs crash
- fault tolerance to sns crash
- queue operations affected by visibility timeout
- coroutines simple usage

# Proposed solution

The solution developed makes use of the following

## Notification Layer
An SNS service listens on the topic and republish on three different queue (Shipping; Accounting; Order)

[Aws Simple Notification Service](support/sns-architecture.png)

## Queue Layer
An SQS service manages 5 different queues (Shipping; Accounting; Order, Marketing and Pricing_Policy)

[Aws Simple Queue Service](support/sqs-architecture.png)

## Data Layer
An S3 service offers the storage service

## Orders API
The following API/Topic/Queue have been built that contains the following endpoints:

| Endpoint          | Medhod | Description                                                                          | Availability |
|-------------------|--------|--------------------------------------------------------------------------------------|--------------|
| /orders           | POST   | Receives orders in json format and delivers in queue 'Marketing' and topic 'Orders'  | Done         |
|                   |        | (it also save the payload in the S3 GucciBucket)                                     |              |
| /orders/dashboard | GET    | Returns orders summary by showing country, total orders, total amount (data from S3) | Done         |
| /orders/shipping  | GET    | Returns order shipping details (data from S3)                                        | Done         |

## Integration layer
This layer manages the following operations:

- listens on a specific queue (Marketing) and then decides if the item price need to be changed 
and if then sends a message to a specific queue (Pricing_Policy)
- listens over the three queues (Accounting, Shipping, Orders) for then forwarding the request to an external system (HTTP)

## Libraries used and functions used

- publish on topic (with amazon aws standard lib)
- send/receive/delete on queue (with amazon aws standard lib)
- create bucket/insert key/read key and its content
- routes get and post
- http client over get/post api
- data type conversion to/from json/httpmessage/message

## Draw.io diagram

[System Diagram](support/GucciDemo.jpg)

## Stack

Execute the container with compose

>docker compose -f compose.yml up
>docker compose -f compose.yml down

docker --version
Docker version 20.10.22, build 3a2c30b

docker-compose --version
docker-compose version 1.25.1, build a82fef07

docker compose version
Docker Compose version v2.14.1

Execute the kotlin application:

- Orders.kt
- OrdersClient.kt
- ExternalSystems.kt

Queue can be monitored from here: http://localhost:9325/

## Payload that represents Orders
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

## TODO
sqsclient -> async with await
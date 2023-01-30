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

| Endpoint          | Medhod | Description                                                                         |
|-------------------|--------|-------------------------------------------------------------------------------------|
| /orders           | POST   | Receives orders in json format and delivers in queue 'Marketing' and topic 'Orders' | 
|                   |        | (it also save the payload in the S3 GucciBucket)                                    |
| /orders/dashboard | GET    | Returns orders summary by showing country, total orders, total amount               |
| /orders/pricing   | GET    | Returns current item price change direction                                         |
| /orders/shipping  | GET    | Returns order shipping details looping over the S3 GucciBucket key                  |

## Integration layer
This layer manages the following operations:
- listens on a specific queue (Marketing) and then decides if the item price need to be changed 
and if then sends a message to a specific queue (Pricing_Policy)
- listens on a specific queue (Shipping) and then ask for immediate delivery by sending a message to a specific queue (Shipping_it) 

## Operazioni eseguite

- publish su topic (con lib aws standard)
- send/receive/delete su queue (con lib aws standard)
- creazione bucket/inserimento key/lettura key e suo contenuto
- routes get e post
- http client verso api get/post
- data type conversion to/from json/httpmessage/message

## Errors
1)
gestione eccezioni, se pubTopic fallisce allora il loop si interrompe (Orders.kt)
2)
inline function
3)
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

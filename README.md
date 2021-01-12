
# Description
Order Management System

This allows placing Orders for items and having them delivered.


# Instructions to run the simulation
Note: Must install maven and JDK11 to build and run the program
```mvn spring-boot:run```

Run the simulated orders using python3:
```python3 src/simulator/simulator.py Downloads/dispatch_orders.json```


# Configuration
File ```src/main/application.yml``` has parameters to configure the
following:

## Port numbers
Main server port is server.port
Jobrunr dashboard is at ```org.jobrunr.dashboard.port```

## Polling interval
Polling interval in seconds is ```org.jobrunr.background-job-server.poll_interval```

## Worker threads
Number of worker threads is ```org.jobrunr.background-job-server.worker_count```

## Courier strategy
Courier strategy is ```ordersystem.courier.strategy```
### Strategy matched
Matched​: a courier is dispatched for a specific order and may only pick up that order
### Strategy fifo
First-in-first-out​: a courier picks up the next available order upon
arrival. If there are multiple orders available, pick up an arbitrary
order. If there are no available orders, couriers wait for the next
available one. When there are multiple couriers waiting, the next
available order is assigned to the earliest​ a​ rrived courier.

## Courier travel time
Min courier travel time is ```ordersystem.courier.min_time_seconds```
Max courier travel time is ```ordersystem.courier.max_time_seconds```


# Architecture

## External API
### ItemController
ItemController	maintains the menu for what is available
This includes the item preparation times as well
### OrderController
OrderController handles order creation and submission
An order may have multiple items in the order

## Repository
### OrderRepository
OrderRepository maintains the orders while being placed and processed.
This was created to make it easier for services need to access the
##orders directly.

## Services

### ItemService
ItemService maintains the items in the menu.
This also serves as the repository for the menu.

### OrderService
OrderService handles order creation and placing.
Work for the submitted orders are taken over by TaskService.

### TaskService
TaskService handles order submission and preparing items.
Each preparation is a thread that sleeps for the scheduled amount of time.
Preparation scheduling attempts to have all items in the order ready at
the same time.  This scheduling is approximate.
Whenever an order is submitted, a courier is dispatched at that time.

### CourierService
CourierService handles orders that are ready for delivery.
It is notified when orders are ready and when couriers arrive.
There are two implementations to handle the strategies.
#### MatchedCourierService
When the order is ready, it checks whether the courier arrived.
When the courier arrives, it checks whether the order is ready.
When both are ready, the order is delivered.
#### FifoCourierService
Orders that arrive are put into a queue.
Couriers that arrive wait till they pick up an order.

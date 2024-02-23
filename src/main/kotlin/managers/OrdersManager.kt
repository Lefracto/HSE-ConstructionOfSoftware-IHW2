package managers

import dataClasses.Meal
import dataClasses.Order
import dataClasses.OrderStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import supportModules.DataSaver

private const val countProcessedAtSameTimeOrders = 3

class OrdersManager(statsManager: StatsManager) {
    private val orders: MutableList<Pair<Order, Job?>> = mutableListOf()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val ordersMutex = Mutex()
    private var jobsCount = 0

    private fun extractNextOrderToCook(): Pair<Order, Job?>? {
        val order = orders.find { pair -> pair.second == null }
        if (order != null)
            orders.remove(order)
        return order
    }

    private fun decreaseJobsCount() {
        jobsCount--
        val nextOrder: Pair<Order, Job?> = extractNextOrderToCook() ?: return

        nextOrder.first.orderStatus = OrderStatus.IsCooking
        val job = coroutineScope.launch {
            cookOrder(nextOrder.first)
        }
        orders.add(Pair(nextOrder.first, job))
        jobsCount++
    }

    private fun getValidOrderInput(userId: Int, orderId: Int): Pair<Order, Job?>? {
        val customersOrders = orders.filter { pair -> pair.first.customerId == userId }
        if (customersOrders.isEmpty()) {
            println("You have no orders!")
            return null
        }

        val order = customersOrders.find { order -> order.first.id == orderId }
        if (order == null) {
            println("You do not have order with such id!")
            return null
        }
        return order
    }

    private suspend fun cookOrder(cookingOrder: Order) = try {
        var i = 0
        var countMeals: Int
        ordersMutex.withLock {
            countMeals = cookingOrder.meals.size
        }
        var delayTime: Int
        while (i < countMeals) {
            ordersMutex.withLock {
                delayTime = cookingOrder.meals[i].preparationTime
            }
            delay(delayTime * 1000L)

            ordersMutex.withLock {
                countMeals = cookingOrder.meals.size
            }
            i++
        }
        ordersMutex.withLock {
            val orderToUpdate = orders.find { it.first.id == cookingOrder.id }
            orderToUpdate?.first?.orderStatus = OrderStatus.Finished
        }
        decreaseJobsCount()
    } catch (e: CancellationException) {
        ordersMutex.withLock {
            orders.find { order -> order.first.id == cookingOrder.id }!!.first.orderStatus = OrderStatus.Canceled
        }
    }

    fun addOrder(menu: MutableList<Meal>, customerId: Int) {
        try {
            print("Enter meals id using \',\' (like this: 0, 1, 2) : ")
            val orderedMeals = Meal.readMealsIds(menu) ?: return
            val order = Order(orderedMeals, OrderStatus.InQueue, customerId)

            if (jobsCount < countProcessedAtSameTimeOrders) {
                order.orderStatus = OrderStatus.IsCooking
                val job = coroutineScope.launch {
                    cookOrder(order)
                }

                orders.add(Pair(order, job))
                jobsCount++
            } else {
                orders.add(Pair(order, null))
            }
            println("Order with id ${order.id} has been added successfully! ")

        } catch (e: NumberFormatException) {
            println("Invalid input format. Please enter meal ids separated by commas.")
        }
    }

    fun checkOrderStatus(userId: Int) {
        val orderId = DataSaver.getIntInput("Enter order id: ")
        val order = getValidOrderInput(userId, orderId) ?: return
        println("You order with id $orderId has next status: ${order.first.orderStatus}")
    }

    fun cancelOrder(userId: Int) {
        val orderId = DataSaver.getIntInput("Enter order id: ")

        val order = getValidOrderInput(userId, orderId) ?: return

        if (order.first.orderStatus == OrderStatus.Paid || order.first.orderStatus == OrderStatus.Finished) {
            println("You can't cancel finished order!")
            return
        }

        if (order.second != null)
            order.second!!.cancel()
        else
            order.first.orderStatus = OrderStatus.Canceled

        println("Order with id ${order.first.id} has been canceled.")
    }

    fun editOrder(menu: MutableList<Meal>, customerId: Int) {
        val orderId = DataSaver.getIntInput("Enter order id: ")
        val order = getValidOrderInput(customerId, orderId) ?: return

        print("Enter meals id using \',\' (like this: 0, 1, 2) : ")
        val orderedMeals = Meal.readMealsIds(menu) ?: return
        orderedMeals.forEach { meal -> order.first.meals.add(meal) }
        println("Meals have been added successfully!")
    }

    fun payForTheOrder(userId: Int) {
        val orderId = DataSaver.getIntInput("Enter order id: ")
        val order = getValidOrderInput(userId, orderId) ?: return
        order.first.orderStatus = OrderStatus.Paid
        println("You order with id $orderId and cost ${order.first.meals.sumOf { meal -> meal.price }} has been paid")
    }

    fun rateOrder(userId: Int) {
        val orderId = DataSaver.getIntInput("Enter order id: ")
        val order = getValidOrderInput(userId, orderId) ?: return
        if (order.first.orderStatus != OrderStatus.Paid) {
            println("You can't rate unpaid order!")
            return
        }
        var dishesToRate = Meal.readMealsIds(order.first.meals) ?: return

        for (dish in dishesToRate) {
            val rating = DataSaver.getIntInput("Enter rating for dish $dish (from 1 to 5): ", 6, 0, true)
            // attach rating to meal
        }
    }

    fun cancelAllOrders() {
        for (order in orders) {
            if (order.first.orderStatus != OrderStatus.Canceled && order.second != null) {
                order.second!!.cancel("")
            }
        }
        println("All orders have been canceled.")
    }
}
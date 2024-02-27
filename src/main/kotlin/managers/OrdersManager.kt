package managers

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import stuff.Meal
import stuff.Order
import stuff.OrderStatus
import supportModules.IoHelper

private const val countProcessedAtSameTimeOrders = 3

private const val cancellationUnfinishedOrderMessage = "You can't cancel finished order!"
private const val allOrdersCancellationText = "All orders have been canceled."
private const val thanksForRateMessage = "Thank you for rating us!"
private const val enterCommentForMealMessage = "Enter a comment for this meal: "
private const val enteringRateForMealMessage = "Enter meals id to rate using \',\' (like this: 0, 1, 2) : "
private const val enteringMealsIdMessage = "Enter meals id using \',\' (like this: 0, 1, 2) : "
private const val addingMealErrorMessage = "Invalid input format. Please enter meal ids separated by commas."
private const val rateUnpaidOrderMessage = "You can't rate unpaid order!"
private const val enteringOrderIdMessage = "Enter order id: "
private const val noOrdersMessage = "You have no orders!"
private const val noOrdersWithSuchIdMessage = "You do not have order with such id!"
private const val successfulAddingMealsMessage = "Meals have been added successfully!"
private const val orderedMealsMessage = "You have ordered next meals: "
private const val noOrdersToRateMessage = "You have no orders to rate!"
private const val possibleOrdersToRateMessage = "You can rate next orders: "

class OrdersManager(private var statsManager: StatsManager) {
    private val orders: MutableList<Pair<Order, Job?>> = mutableListOf()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val ordersMutex = Mutex()
    private var jobsCount = 0

    private var wasQueueAdvance: Boolean = false

    // Priorities are here
    private fun extractNextOrderToCook(): Pair<Order, Job?>? {
        val ordersInQueue = orders.filter { pair -> pair.second == null }

        if (ordersInQueue.isEmpty()) {
            return null
        }

        val newOrder = if (wasQueueAdvance) {
            ordersInQueue[0]
        } else {
            ordersInQueue.minBy { order -> order.first.meals.sumOf { meal -> meal.preparationTime } }
        }
        wasQueueAdvance = !wasQueueAdvance

        orders.remove(newOrder)
        return newOrder
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
            println(noOrdersMessage)
            return null
        }

        val order = customersOrders.find { order -> order.first.id == orderId }
        if (order == null) {
            println(noOrdersWithSuchIdMessage)
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
            if (menu.isEmpty())
                return
            print(enteringMealsIdMessage)
            val orderedMeals = Meal.readMealsIds(menu) ?: return
            orderedMeals.map { meal -> meal.countBookings++ }
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
            statsManager.ordersCount++
            println("Order with id ${order.id} has been added successfully! ")

        } catch (e: NumberFormatException) {
            println(addingMealErrorMessage)
        }
    }

    fun checkOrderStatus(userId: Int) {
        val orderId = IoHelper.getIntInput(enteringOrderIdMessage)
        val order = getValidOrderInput(userId, orderId) ?: return
        println("You order with id $orderId has next status: ${order.first.orderStatus}")
    }

    fun cancelOrder(userId: Int) {
        val orderId = IoHelper.getIntInput(enteringOrderIdMessage)

        val order = getValidOrderInput(userId, orderId) ?: return

        if (order.first.orderStatus == OrderStatus.Paid || order.first.orderStatus == OrderStatus.Finished) {
            println(cancellationUnfinishedOrderMessage)
            return
        }

        if (order.second != null)
            order.second!!.cancel()
        else
            order.first.orderStatus = OrderStatus.Canceled

        println("Order with id ${order.first.id} has been canceled.")
    }

    fun editOrder(menu: MutableList<Meal>, customerId: Int) {
        print("You have next orders: ")
        val possibleOrders = orders.filter { localOrder -> localOrder.first.customerId == customerId }
        possibleOrders.forEach { localOrder -> print("${localOrder.first.id}  ") }
        println()

        val orderId = IoHelper.getIntInput(enteringOrderIdMessage)
        val order = getValidOrderInput(customerId, orderId) ?: return

        if (order.first.orderStatus != OrderStatus.IsCooking && order.first.orderStatus != OrderStatus.InQueue) {
            println("You can add meals only for orders with status IsCooking or InQueue")
            return
        }

        print(enteringMealsIdMessage)
        val orderedMeals = Meal.readMealsIds(menu) ?: return

        orderedMeals.forEach { meal -> order.first.meals.add(meal) }
        println(successfulAddingMealsMessage)
    }

    fun payForTheOrder(userId: Int) {
        val orderId = IoHelper.getIntInput(enteringOrderIdMessage)
        val order = getValidOrderInput(userId, orderId) ?: return

        if (order.first.orderStatus != OrderStatus.Finished) {
            println("You can pay for only finished orders!")
        }

        order.first.orderStatus = OrderStatus.Paid
        val cost = order.first.meals.sumOf { meal -> meal.price }
        statsManager.revenue += cost
        println("You order with id $orderId and cost $cost has been paid")
    }

    fun rateOrder(userId: Int) {
        val ordersToRate =
            orders.filter { order -> order.first.customerId == userId && order.first.orderStatus == OrderStatus.Paid }
        if (ordersToRate.isEmpty()) {
            println(noOrdersToRateMessage)
            return
        } else {
            print(possibleOrdersToRateMessage)
            ordersToRate.forEach { order -> print("${order.first.id} ") }
            println()
        }

        val orderId = IoHelper.getIntInput(enteringOrderIdMessage)
        val order = getValidOrderInput(userId, orderId) ?: return
        if (order.first.orderStatus != OrderStatus.Paid) {
            println(rateUnpaidOrderMessage)
            return
        }

        print(enteringRateForMealMessage)
        val dishesToRate = Meal.readMealsIds(order.first.meals) ?: return

        for (dish in dishesToRate) {
            val rating = IoHelper.getIntInput("Enter rating for dish $dish (from 1 to 5): ", 6, 0, true)
            print(enterCommentForMealMessage)
            val commentForMeal = readlnOrNull() ?: ""
            dish.rating.addRating(rating, commentForMeal)
        }
        println(thanksForRateMessage)
    }

    fun cancelAllOrders() {
        for (order in orders) {
            if (order.first.orderStatus != OrderStatus.Canceled && order.second != null) {
                order.second!!.cancel("")
            }
        }
        println(allOrdersCancellationText)
    }
}
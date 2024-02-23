package managers

import kotlinx.serialization.Serializable

@Serializable
class StatsManager(private val statsFile : String) {
    var revenue: Int = 0
    var ordersCount: Int = 0
    var countCustomers: Int = 0

    fun saveToFile() {

    }

    fun loadFromFile() {

    }

    fun printStats() {
        println("---------- Stats ---------")
        println("Count Customers: $countCustomers")
        println("Total Revenue: $revenue")
        println("Orders Count: $ordersCount")
        println("-------- End Stats -------")
    }
}
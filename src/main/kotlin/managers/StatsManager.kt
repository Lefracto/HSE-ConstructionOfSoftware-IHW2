package managers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class StatsManager(private val statsFile : String) {
    var revenue: Int = 0
    var ordersCount: Int = 0
    var countCustomers: Int = 0

    fun saveToFile() {
        val jsonString = Json.encodeToString(serializer(), this)
        try {
            File(statsFile).writeText(jsonString)
        } catch (e: Exception) {
            println("Error occurred while saving stats to file: ${e.message}")
        }
    }

    fun loadFromFile() {
        try {
            val jsonString = File(statsFile).readText()
            val loadedStats = Json.decodeFromString<StatsManager>(jsonString)
            this.revenue = loadedStats.revenue
            this.ordersCount = loadedStats.ordersCount
            this.countCustomers = loadedStats.countCustomers
        } catch (e: Exception) {
            println("Error occurred while loading stats from file: ${e.message}")
        }
    }
    fun printStats() {
        println("---------- Stats ---------")
        println("Count Customers: $countCustomers")
        println("Total Revenue: $revenue")
        println("Orders Count: $ordersCount")
        println("-------- End Stats -------")
    }
}
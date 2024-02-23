package supportModules

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

object DataSaver {
    val json = Json { prettyPrint = true }
    inline fun <reified T> serializeListToFile(list: List<T>, fileName: String) {
        try {
            val jsonString = json.encodeToString(list)
            File(fileName).writeText(jsonString)
        } catch (e: Exception) {
            println("Error occurred during serialization: ${e.message}")
        }
    }

    inline fun <reified T> deserializeListFromFile(fileName: String): List<T> {
        try {
            val jsonString = File(fileName).readText()
            return json.decodeFromString(jsonString)
        } catch (e: Exception) {
            println("Error occurred during deserialization: ${e.message}")
        }
        return emptyList()
    }

    fun getIntInput(text: String = "", maxValue: Int = 0, minValue: Int = 0, hasLimit: Boolean = false): Int {
        print(text)
        val scanner = Scanner(System.`in`)
        while (true) {
            try {
                val newInt = scanner.nextInt()

                if (newInt < minValue || (newInt > maxValue && hasLimit))
                    throw InputMismatchException()

                return newInt
            } catch (e: InputMismatchException) {
                println("Invalid input! Please enter a valid integer.")
                scanner.nextLine()
            }
        }
    }
}
package stuff

import kotlinx.serialization.Serializable
import supportModules.IdGenerator

@Serializable
data class Meal(
    var id: Int,
    var name: String,
    var price: Int,
    var preparationTime: Int,
    var countBookings: Int,
    var rating: Rating
) {
    companion object {
        val idGenerator = IdGenerator()

        fun readMealsIds(menu : MutableList<Meal>) : MutableList<Meal>? {
            val input = readlnOrNull() ?: ""
            val mealIds = input.split(",").map { it.trim().toInt() }
            val orderedMeals = mutableListOf<Meal>()

            for (mealId in mealIds) {
                val meal = menu.find { it.id == mealId }
                if (meal != null) {
                    orderedMeals.add(meal)
                } else {
                    println("Meal with id $mealId not found.")
                    return null
                }
            }
            return orderedMeals
        }
    }

    constructor(name: String, price: Int, preparationTime: Int, countBookings: Int, rating: Rating) :
            this(idGenerator.generateId(), name, price, preparationTime, countBookings, rating)

    override fun toString(): String {
        return name
    }
}

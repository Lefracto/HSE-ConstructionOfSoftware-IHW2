package managers

import stuff.Meal
import stuff.Rating
import supportModules.IoHelper
import java.util.*

private const val enterMealsIdMessage = "Enter meal's id: "
private const val noMealsInMenuMessage = "There are no meals in the menu."
private const val addingNewMealMessage = "Adding a new meal."
private const val successfulAddingMealMessage = "Meal added successfully!"
private const val askForMealIdForCommentsMessage = "Print meal id to see a comments of visitors: "
private const val enteringMealNameMessage = "Enter meal name: "
private const val enteringMealPriceMessage = "Enter meal price: "
private const val enteringCookTimeMessage = "Enter preparation time (in seconds): "
private const val successfulMealChangeMessage = "Meal has been changed successfully."

class MealsManager(private val mealsFileName: String) {
    private val meals: MutableList<Meal> = mutableListOf()
    private val scanner = Scanner(System.`in`)

    private fun getMealByInput(): Meal? {
        val id = IoHelper.getIntInput(enterMealsIdMessage)
        return meals.find { meal -> meal.id == id }
    }

    fun getMenu(): MutableList<Meal> {
        return meals
    }

    fun addMeal() {
        val scanner = Scanner(System.`in`)
        println(addingNewMealMessage)
        print(enteringMealNameMessage)
        val name = scanner.nextLine()

        val price = IoHelper.getIntInput(enteringMealPriceMessage)
        val preparationTime = IoHelper.getIntInput(enteringCookTimeMessage)

        val newMeal = Meal(name, price, preparationTime, 0, Rating(0.0, 0, mutableListOf()))
        meals.add(newMeal)
        println(successfulAddingMealMessage)
    }

    fun removeMeal() {
        if (meals.isEmpty()) {
            println(noMealsInMenuMessage)
            return
        }

        val meal = getMealByInput() ?: return
        meals.remove(meal)
    }

    fun editMeal() {
        if (meals.isEmpty()) {
            println(noMealsInMenuMessage)
            return
        }

        val meal = getMealByInput() ?: return

        print(enteringMealNameMessage)
        meal.name = scanner.nextLine()

        meal.price = IoHelper.getIntInput(enteringMealPriceMessage)
        meal.preparationTime = IoHelper.getIntInput(enteringCookTimeMessage)

        println(successfulMealChangeMessage)
    }

    fun showMeals(isAdmin: Boolean) {
        if (meals.isEmpty()) {
            println(noMealsInMenuMessage)
            return
        }

        val headerFormat = if (isAdmin) "%-10s %-20s %-10s %-20s %-10s %-15s" else "%-10s %-20s %-10s %-20s"
        println(headerFormat.format("id", "name", "price", "timeToCook(sec)", "bookings", "rating"))

        for (meal in meals) {
            val mealInfo = if (isAdmin) {
                "%-10s %-20s %-10s %-20s %-10s %-15s".format(
                    meal.id,
                    meal.name,
                    meal.price,
                    meal.preparationTime,
                    meal.countBookings,
                    meal.rating.value,
                )
            } else {
                "%-10s %-20s %-10s %-20s".format(
                    meal.id,
                    meal.name,
                    meal.price,
                    meal.preparationTime,
                )
            }
            println(mealInfo)
        }

    }

    fun printMealComments() {
        if (meals.isEmpty()) {
            println(noMealsInMenuMessage)
            return
        }

       // print(askForMealIdForCommentsMessage)
        val meal = getMealByInput() ?: return

        for (text in meal.rating.comments) {
            println("-------- Comment --------")
            println(text)
            println("-----  End Comment -----")
        }
    }

    fun saveToFile() {
        IoHelper.serializeListToFile<Meal>(meals, mealsFileName)
    }

    fun loadFromFile() {
        meals.addAll(IoHelper.deserializeListFromFile<Meal>(mealsFileName))
        if (meals.isNotEmpty())
            Meal.idGenerator.setLastId(meals.maxBy { meals -> meals.id }.id + 1)
    }
}
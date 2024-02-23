package managers

import dataClasses.Meal
import dataClasses.Rating
import dataClasses.User
import supportModules.DataSaver
import java.util.*

class MealsManager(private val mealsFileName: String) {
    private val meals: MutableList<Meal> = mutableListOf()
    private val scanner = Scanner(System.`in`)

    private fun getMealByInput(): Meal? {
        val id = DataSaver.getIntInput("Enter meal's id: ")
        return meals.find { meal -> meal.id == id }
    }

    fun getMenu(): MutableList<Meal> {
        return meals
    }

    fun addMeal() {
        val scanner = Scanner(System.`in`)
        println("Adding a new meal.")
        print("Enter meal name: ")
        val name = scanner.nextLine()

        val price = DataSaver.getIntInput("Enter meal price: ")
        val preparationTime = DataSaver.getIntInput("Enter preparation time (in minutes): ")

        val newMeal = Meal(name, price, preparationTime, 0, Rating(4.0, 10))
        meals.add(newMeal)
        println("Meal added successfully!")
    }

    fun removeMeal() {
        if (meals.isEmpty()) {
            println("There are no meals in menu.")
            return
        }

        val meal = getMealByInput() ?: return
        meals.remove(meal)
    }

    fun editMeal() {
        if (meals.isEmpty()) {
            println("There are no meals in menu.")
            return
        }

        val meal = getMealByInput() ?: return

        print("Enter new meal name: ")
        meal.name = scanner.nextLine()

        meal.price = DataSaver.getIntInput("Enter new meal price: ")
        meal.preparationTime = DataSaver.getIntInput("Enter new preparation time (in minutes): ")

        println("Meal has been changed successfully.")
    }

    fun showMeals() {
        if (meals.isNotEmpty()) {
            println("%-10s %-20s %-10s %-20s %-10s".format("id", "name", "price", "timeToCook(min)", "countBookings"))
            for (meal in meals) {
                println(
                    "%-10s %-20s %-10s %-20s %-10s".format(
                        meal.id,
                        meal.name,
                        meal.price,
                        meal.preparationTime,
                        meal.countBookings
                    )
                )
            }
        } else {
            println("There are no meals in the menu.")
        }
    }

    fun saveToFile() {
        DataSaver.serializeListToFile<Meal>(meals, mealsFileName)
    }

    fun loadFromFile() {
        meals.addAll(DataSaver.deserializeListFromFile<Meal>(mealsFileName))
        if (meals.isNotEmpty())
            User.idGenerator.setLastId(meals.maxBy { meals -> meals.id }.id + 1)
    }
}
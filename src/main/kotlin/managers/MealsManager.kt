package managers

import java.util.*
class MealsManager {

    private val meals: MutableList<Meal> = mutableListOf()

    fun addMeal() {
        val scanner = Scanner(System.`in`)
        println("Adding a new meal:")
        print("Enter meal name: ")
        val name = scanner.nextLine()

        print("Enter meal price: ")
        val price = getIntInput()

        print("Enter preparation time (in minutes): ")
        val preparationTime = getIntInput()

        val newMeal = Meal(name, price, preparationTime)
        meals.add(newMeal)
        println("Meal added successfully!")
    }
    private fun getIntInput(): Int {
        val scanner = Scanner(System.`in`)
        while (true) {
            try {
                val newInt = scanner.nextInt()

                if (newInt <= 0)
                    throw InputMismatchException()

                return newInt
            } catch (e: InputMismatchException) {
                println("Invalid input! Please enter a valid integer.")
                scanner.nextLine()
            }
        }
    }

    fun removeMeal() {
        if (meals.isEmpty()) {
            println("There are no meals in menu.")
            return
        }

        print("Enter meal's id: ")
        val id = getIntInput()
        val meal = meals.find { meal -> meal.id == id }
        if (meal == null) {
            println("There are no meal with such id.")
            return
        }
        meals.remove(meal)
    }
    fun editMeal() {
        if (meals.isEmpty()) {
            println("There are no meals in menu.")
            return
        }

        val scanner = Scanner(System.`in`)
        print("Enter meal's id: ")
        val id = getIntInput()
        val meal = meals.find { meal -> meal.id == id }
        if (meal == null) {
            println("There are no meal with such id.")
        }

        print("Enter new meal name: ")
        meal!!.name = scanner.nextLine()

        print("Enter new meal price: ")
        meal.price = getIntInput()

        print("Enter new preparation time (in minutes): ")
        meal.preparationTime = getIntInput()

        println("Meal has been changed successfully.")
    }
    fun showMeals() {
        if (meals.isNotEmpty()) {
            println("id\t\t\tname\t\t\tprice\t\t\ttimeToCook(min)")
            for (meal in meals) {
                println(meal)
            }
        } else {
            println("There are no meals in menu.")
        }
    }
}
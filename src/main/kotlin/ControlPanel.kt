
import authentication.Authorizer
import authentication.User
import managers.MealsManager


class ControlPanel {
    private var exitProgramCommandText = "exit"
    private var incorrectCommandMessage = "Incorrect command!"

    private val commandList: MutableList<Command> = mutableListOf()
    private val authorizer: Authorizer = Authorizer("users.json")
    private val mealsManager: MealsManager = MealsManager()
    private var currentUser: User? = null


    private fun printHelp() {
        println("Some help")
    }

    private fun createCommandsMap() {
        // customer's commands
        commandList.add(Command("help", false) { printHelp() })
        commandList.add(Command("log out", false) { currentUser = null })


        // admins commands
        commandList.add(Command("log out", true) { currentUser = null })
        commandList.add(Command("register admin", true) { authorizer.registerUser(true) })
        commandList.add(Command("guide", true) { printHelp() })
        commandList.add(Command("add meal", true) { mealsManager.addMeal() })
        commandList.add(Command("show meals", true) { mealsManager.showMeals() })
        commandList.add(Command("edit meal", true) { mealsManager.editMeal() })
        commandList.add(Command("remove meal", true) { mealsManager.removeMeal() })


        /*commandList.add(Command("remove meal", true) { authorizer.tryAuthorize() })
        commandList.add(Command("show stats", true) { authorizer.tryAuthorize() })



        // customer commands
        commandList.add(Command("make order", false) { authorizer.tryAuthorize() })
        commandList.add(Command("check order", false) { authorizer.tryAuthorize() })
        commandList.add(Command("pay order", false) { authorizer.tryAuthorize() })
        commandList.add(Command("cancel order", false) { authorizer.tryAuthorize() })
        commandList.add(Command("change order", false) { authorizer.tryAuthorize() }) */
    }


    private fun printEnterText() {
        println("\nEnter command: ")
    }


    private fun processCommand(commandText: String): Boolean {
        val usersCommands = commandList.filter { user -> user.isAdminCommand == currentUser?.isAdmin }
        val command = usersCommands.find { c -> c.command == commandText } ?: return false

        if (command.isAdminCommand == currentUser?.isAdmin)
            command.action.invoke()
        else
            println("Error of access level.")

        return true
    }

    private fun checkAuthorization(): Boolean {
        currentUser = currentUser ?: this.authorizer.requestUser(false)
        return currentUser != null
    }

    fun start() {
        createCommandsMap()
        loadData()

        var command: String
        do {
            if (!checkAuthorization())
                break

            printEnterText()
            command = readlnOrNull().toString()

            if (!processCommand(command) && command != exitProgramCommandText)
                println(incorrectCommandMessage)

        } while (command != exitProgramCommandText)

        println("Exiting program...")
        saveData()
    }

    private fun saveData() {
        authorizer.saveToFile()
    }

    private fun loadData() {
        authorizer.loadFromFile()
    }
}
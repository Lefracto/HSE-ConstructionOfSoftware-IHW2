import authentication.Authorizer
import dataClasses.Command
import dataClasses.User
import managers.MealsManager
import managers.OrdersManager
import managers.StatsManager

private const val exitProgramCommandText = "exit"
private const val incorrectCommandMessage = "Incorrect command!"

private const val usersFileName = "users.json"
private const val mealsFileName = "meals.json"
private const val statsFileName = "stats.json"

private const val helpFileName = "help.txt"
private const val guideFileName = "guide.txt"

class ControlPanel {
    private val commandList: MutableList<Command> = mutableListOf()


    private val authorizer: Authorizer = Authorizer(usersFileName)
    private val mealsManager: MealsManager = MealsManager(mealsFileName)
    private val statsManager: StatsManager = StatsManager(statsFileName)
    private val ordersManager: OrdersManager = OrdersManager(statsManager)

    private var currentUser: User? = null


    private fun printHelp() {
        println("Some help")
    }

    private fun createCommandsMap() {
        // customer's commands
        commandList.add(Command("help", false) { printHelp() })
        commandList.add(Command("log out", false) { currentUser = null })
        commandList.add(
            Command(
                "order",
                false
            ) { mealsManager.showMeals(); ordersManager.addOrder(mealsManager.getMenu(), currentUser!!.id) })
        commandList.add(Command("check", false) {
            ordersManager.checkOrderStatus(
                currentUser!!.id,
            )
        })
        commandList.add(Command("cancel", false) {
            ordersManager.cancelOrder(
                currentUser!!.id,
            )
        })
        commandList.add(Command("rate", false) { })
        commandList.add(Command("pay", false) {
            ordersManager.payForTheOrder(
                currentUser!!.id,
            )
        })
        commandList.add(Command("add meals", false) {
            ordersManager.editOrder(
                mealsManager.getMenu(),
                currentUser!!.id
            )
        })
        commandList.add(Command("rate dish", false) { ordersManager.rateOrder(currentUser!!.id) })


        // admins commands
        commandList.add(Command("log out", true) { currentUser = null })
        commandList.add(Command("register admin", true) { authorizer.registerUser(true) })
        commandList.add(Command("guide", true) { printHelp() })
        commandList.add(Command("add meal", true) { mealsManager.addMeal() })
        commandList.add(Command("show meals", true) { mealsManager.showMeals() })
        commandList.add(Command("edit meal", true) { mealsManager.editMeal() })
        commandList.add(Command("remove meal", true) { mealsManager.removeMeal() })


        //commandList.add(dataClasses.Command("show stats", true) { authorizer.tryAuthorize() })

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
        mealsManager.saveToFile()
        ordersManager.cancelAllOrders()
    }

    private fun loadData() {
        authorizer.loadFromFile()
       // mealsManager.loadFromFile()
    }
}
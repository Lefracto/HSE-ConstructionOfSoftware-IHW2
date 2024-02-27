
import authentication.Authorizer
import authentication.User
import managers.MealsManager
import managers.OrdersManager
import managers.StatsManager
import stuff.Command
import supportModules.IoHelper

private const val exitProgramCommandText = "exit"
private const val incorrectCommandMessage = "Incorrect command!"

private const val usersFileName = "savedData\\users.json"
private const val mealsFileName = "savedData\\meals.json"
private const val statsFileName = "savedData\\stats.json"

private const val helpFileName = "savedData\\help.txt"
private const val guideFileName = "savedData\\guide.txt"

private const val exitingProgramMessage = "Exiting program..."

class ControlPanel {
    private val commandList: MutableList<Command> = mutableListOf()

    private val statsManager: StatsManager = StatsManager(statsFileName)
    private val authorizer: Authorizer = Authorizer(usersFileName, statsManager)
    private val mealsManager: MealsManager = MealsManager(mealsFileName)
    private val ordersManager: OrdersManager = OrdersManager(statsManager)

    private var currentUser: User? = null

    private fun printHelp(fileName: String) {
        println(IoHelper.readFromFile(fileName))
    }
    private fun createCommandsMap() {
        // customer's commands
        commandList.add(Command("help", false) { printHelp(helpFileName) })
        commandList.add(Command("log out", false) { currentUser = null })
        commandList.add(
            Command(
                "order",
                false
            ) { mealsManager.showMeals(false); ordersManager.addOrder(mealsManager.getMenu(), currentUser!!.id) })
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
        commandList.add(Command("rate", false) { ordersManager.rateOrder(currentUser!!.id) })
        commandList.add(Command("menu", false) { mealsManager.showMeals(false) })


        // admins commands
        commandList.add(Command("log out", true) { currentUser = null })
        commandList.add(Command("register admin", true) { authorizer.registerUser(true) })
        commandList.add(Command("guide", true) { printHelp(guideFileName) })
        commandList.add(Command("add meal", true) { mealsManager.addMeal() })
        commandList.add(Command("menu", true) { mealsManager.showMeals(true) })
        commandList.add(Command("edit meal", true) { mealsManager.editMeal() })
        commandList.add(Command("remove meal", true) { mealsManager.removeMeal() })
        commandList.add(Command("show stats", true) { statsManager.printStats() })
        commandList.add(Command("show comments", true) { mealsManager.printMealComments() })
    }
    private fun printEnterText() {
        println("\nEnter command: ")
    }
    private fun processCommand(commandText: String): Boolean {
        val usersCommands = commandList.filter { user -> user.isAdminCommand == currentUser?.isAdmin }
        val command = usersCommands.find { c -> c.command == commandText } ?: return false
        command.action.invoke()
        return true
    }
    private fun checkAuthorization(): Boolean {
        currentUser = currentUser ?: this.authorizer.requestUser(false)
        return currentUser != null
    }
    private fun saveData() {
        statsManager.saveToFile()
        authorizer.saveToFile()
        mealsManager.saveToFile()
        ordersManager.cancelAllOrders()
    }
    private fun loadData() {
        authorizer.loadFromFile()
        mealsManager.loadFromFile()
        statsManager.loadFromFile()
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

        println(exitingProgramMessage)
        saveData()
    }
}
import authentication.Authorizer
import authentication.User


class ControlPanel {
    private var exitProgramCommandText = "exit"
    private var incorrectCommandMessage = "Incorrect command!"
    // private var adminLogInCommandText = "admin"
    // private var exitAdminCommandText = "log out"
    // private var notAdminMessage = "You are not admin!"


    private val commandList: MutableList<Command> = mutableListOf()
    private val authorizer: Authorizer = Authorizer()
    private var currentUser: User? = null


    private fun printHelp() {
        println("Some help")
    }

    private fun createCommandsMap() {
        // commandList.add(Command("admin", false) { authorizer.requestAuthorization() })
        commandList.add(Command("help", false) { printHelp() })
        commandList.add(Command("log out", false) { currentUser = null })
        /*commandList.add(Command("add meal", true) { authorizer.tryAuthorize() })
        commandList.add(Command("remove meal", true) { authorizer.tryAuthorize() })
        commandList.add(Command("change meal", true) { authorizer.tryAuthorize() })
        commandList.add(Command("print meals", true) { authorizer.tryAuthorize() })
        commandList.add(Command("show stats", true) { authorizer.tryAuthorize() })

        // customer commands
        commandList.add(Command("make order", false) { authorizer.tryAuthorize() })
        commandList.add(Command("check order", false) { authorizer.tryAuthorize() })
        commandList.add(Command("pay order", false) { authorizer.tryAuthorize() })
        commandList.add(Command("cancel order", false) { authorizer.tryAuthorize() })
        commandList.add(Command("change order", false) { authorizer.tryAuthorize() }) */
    }


    private fun printEnterText() {
        println("You can call guide by 'help' command)\n Enter command: ")
    }


    private fun processCommand(commandText: String): Boolean {
        val command = commandList.find { c -> c.command == commandText } ?: return false
        command.action.invoke()
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
            if(!checkAuthorization())
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
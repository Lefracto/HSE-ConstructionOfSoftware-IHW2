package authentication

import managers.StatsManager
import supportModules.IoHelper
import java.util.*

private const val startText = "Welcome! Enter necessary command (login, register, exit) :"
private const val newUserText = "Registering a new user:"
private const val invalidChoiceText = "Invalid choice."
private const val successfulLoginText = "OK"
private const val incorrectPasswordText = "Incorrect password!"
private const val incorrectLoginText = "Incorrect login!"
private const val askForLoginText = "Enter your login: "
private const val askForPasswordText = "Enter your password: "
private const val loggingText = "Logging in:"
private const val registerCommandText = "register"
private const val loginCommandText = "log in"
private const val exitCommandText = "exit"

class Authorizer(private val usersFileName: String, private val statsManager: StatsManager) {

    private val users: MutableList<User> = mutableListOf()
    private val scanner = Scanner(System.`in`)

    private fun getUserByData(login: String, password: String): User? {
        val user = users.find { user -> user.login == login }
        if (user != null) {
            val storedPasswordHash = user.passwordHash
            val providedPasswordHash = password.hashCode()
            if (storedPasswordHash == providedPasswordHash) {
                println(successfulLoginText)
                return user
            }

            println(incorrectPasswordText)
            return null
        }
        println(incorrectLoginText)
        return null
    }
    private fun loginUser(): User? {
        println(loggingText)
        val (login, password) = requestCredentials()
        return getUserByData(login, password)
    }
    private fun requestCredentials(): Pair<String, String> {
        val scanner = Scanner(System.`in`)

        print(askForLoginText)
        val login = scanner.nextLine()

        print(askForPasswordText)
        val password = scanner.nextLine()

        return login to password
    }
    private fun addUser(user: User) {
        users.add(user)
    }
    fun registerUser(isAdmin: Boolean): User {
        println(newUserText)
        val (login, password) = requestCredentials()
        val user = User(login, password, isAdmin)
        addUser(user)
        if (!isAdmin)
            statsManager.countCustomers++
        return user
    }
    fun requestUser(isAdmin: Boolean): User? {
        while (true) {
            println(startText)

            val choice = scanner.nextLine()

            when (choice) {
                registerCommandText -> return registerUser(isAdmin)
                loginCommandText -> {
                    val user = loginUser()
                    if (user != null)
                        return user
                }

                exitCommandText -> return null
                else -> println(invalidChoiceText)
            }
        }
    }
    fun saveToFile() {
        IoHelper.serializeListToFile<User>(users, usersFileName)
    }
    fun loadFromFile() {
        users.addAll(IoHelper.deserializeListFromFile<User>(usersFileName))
        if (users.isNotEmpty())
            User.idGenerator.setLastId(users.maxBy { user -> user.id }.id + 1)
    }
}
package authentication

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*


class Authorizer {
    private val users: MutableList<User> = mutableListOf()
    private val scanner = Scanner(System.`in`)
    private val json1 = Json { prettyPrint = true }


    private var savedUsersFileName = "users.json"
    private var successfulLoginText = "OK"
    private var incorrectPasswordText = "Incorrect password!"
    private var incorrectLoginText = "Incorrect login!"

    private var askForLoginText = "Enter your login: "
    private var askForPasswordText = "Enter your password: "


    private fun getUserByData(login: String, password: String): User? {
        val user = users.find { user -> user.login == login }
        if (user != null) {
            val storedPasswordHash = user.passwordHash
            val providedPasswordHash = password.hashCode()
            if (storedPasswordHash == providedPasswordHash) {
                println(successfulLoginText)
                return user;
            }

            println(incorrectPasswordText)
            return null
        }
        println(incorrectLoginText)
        return null
    }

    private fun registerUser(isAdmin: Boolean): User {
        println("Registering a new user:")
        val (login, password) = requestCredentials()
        val user = User(login, password, isAdmin)
        addUser(user)
        return user
    }

    private fun loginUser(): User? {
        println("Logging in:")
        val (login, password) = requestCredentials()
        return getUserByData(login, password)
    }

    private fun requestCredentials(): Pair<String, String> {
        val scanner = Scanner(System.`in`)

        print("Enter your login: ")
        val login = scanner.nextLine()

        print("Enter your password: ")
        val password = scanner.nextLine()

        return login to password
    }

    fun requestUser(isAdmin: Boolean): User? {
        while (true) {
            println("Welcome! What would you like to do (enter command)?")
            println("1. Register")
            println("2. Login")
            println("3. Exit")


            val choice = scanner.nextLine()

            when (choice) {
                "1" -> return registerUser(isAdmin)
                "2" -> {
                    val user = loginUser()
                    if (user != null)
                        return user
                }
                "3" -> return null
                else -> println("Invalid choice.")
            }
        }
    }

    fun addUser(user: User) {
        users.add(user)
    }

    fun saveToFile() {
        val json = json1.encodeToString(users)
        File(savedUsersFileName).writeText(json)
    }

    fun loadFromFile() {
        val file = File(savedUsersFileName)
        if (file.exists()) {
            val json = file.readText()
            val userList = Json.decodeFromString<List<User>>(json)
            users.clear()
            users.addAll(userList)
            User.setLastId(users.maxBy { user -> user.id }.id + 1)
        } else {
            println("File not found")
        }
    }
}
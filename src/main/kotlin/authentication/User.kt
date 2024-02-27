package authentication

import kotlinx.serialization.Serializable
import supportModules.IdGenerator

@Serializable
data class User(val id: Int, val login: String, val passwordHash: Int, val isAdmin: Boolean) {
    companion object {
        val idGenerator = IdGenerator()
    }

    constructor(login: String, password: String, isAdmin: Boolean) :
            this(idGenerator.generateId(), login, password.hashCode(), isAdmin)
}
package authentication

import kotlinx.serialization.Serializable

@Serializable
class User(val id: Int, val login: String, val passwordHash: Int, val isAdmin: Boolean) {
    companion object {
        private var lastId = 0

        fun generateId(): Int {
            return (lastId + 1)
        }

        fun setLastId(id : Int) {
            lastId = id
        }
    }

    constructor(login: String, password: String, isAdmin: Boolean) :
            this(generateId(), login, password.hashCode(), isAdmin)
}
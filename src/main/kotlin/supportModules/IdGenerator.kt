package supportModules

import kotlinx.serialization.Serializable

@Serializable
class IdGenerator {
    private var lastId = 0
    fun generateId(): Int {
        return ++lastId
    }

    fun setLastId(id: Int) {
        lastId = id
    }
}
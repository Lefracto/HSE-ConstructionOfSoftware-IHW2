package managers

data class Meal(
    val id: Int,
    var name: String,
    var price: Int,
    var preparationTime: Int,
) {
    companion object {
        private var lastId = 0

        fun generateId(): Int {
            return ++lastId
        }

        fun setLastId(id: Int) {
            lastId = id
        }
    }

    constructor(name: String, price: Int, preparationTime: Int)
            : this(generateId(), name, price, preparationTime)
    override fun toString(): String {
        return "$id\t\t\t'$name'\t\t\t$price\t\t\t$preparationTime"
    }
}

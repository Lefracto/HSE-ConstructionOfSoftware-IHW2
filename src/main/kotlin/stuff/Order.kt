package stuff

import kotlinx.serialization.Serializable
import supportModules.IdGenerator

@Serializable
data class Order(val id: Int, val meals: MutableList<Meal>, var orderStatus: OrderStatus, val customerId: Int) {
    companion object {
        var idGenerator = IdGenerator()
    }

    constructor(meals:  MutableList<Meal>, orderStatus: OrderStatus, customerId: Int) :
            this(idGenerator.generateId(), meals, orderStatus, customerId)

    override fun toString(): String {
        return super.toString()
    }
}
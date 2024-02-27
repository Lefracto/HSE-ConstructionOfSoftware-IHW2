package dataClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Rating {
    var comments : MutableList<String> = mutableListOf()

    @SerialName("rating")
    var value: Double = 0.0
        private set

    @SerialName("countRates")
    private var countRates = 0
    fun addRating(newRate : Int, newComment : String) {
        value = (value * countRates + newRate) / (countRates + 1)
        comments.add(newComment)
        countRates++
    }

    constructor(rating: Double, countRates: Int, comments : MutableList<String>) {
        this.countRates = countRates
        this.comments = comments
        this.value = rating
    }
}

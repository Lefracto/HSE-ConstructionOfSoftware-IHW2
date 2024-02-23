package dataClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Rating {

    @SerialName("rating")
    var rating: Double = 0.0
        private set

    @SerialName("countRates")
    private var countRates = 0
    fun addRating(newRate : Int) {
        rating = (rating * countRates + newRate) / (countRates + 1)
        countRates++
    }

    constructor(rating: Double, countRates: Int) {
        this.rating = rating
        this.countRates = countRates
    }
}

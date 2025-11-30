package dev.miguelehr.casinoblackjackpc2.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- MODELOS DE RESPUESTA ---

data class DeckResponse(
    val success: Boolean,
    @SerializedName("deck_id") val deckId: String,
    val remaining: Int,
    val shuffled: Boolean
)

data class Card(
    val value: String,
    val suit: String,
    val image: String
)

data class DrawResponse(
    val success: Boolean,
    @SerializedName("deck_id") val deckId: String,
    val cards: List<Card>,
    val remaining: Int
)

// --- INTERFAZ DE LA API ---

interface DeckApiService {

    // Baraja nueva y mezclada
    @GET("deck/new/shuffle/")
    suspend fun newShuffledDeck(
        @Query("deck_count") deckCount: Int = 1
    ): DeckResponse

    // Pedir cartas
    @GET("deck/{deck_id}/draw/")
    suspend fun drawCards(
        @Path("deck_id") deckId: String,
        @Query("count") count: Int
    ): DrawResponse
}

// --- SINGLETON DE RETROFIT ---

object DeckApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://deckofcardsapi.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: DeckApiService = retrofit.create(DeckApiService::class.java)
}
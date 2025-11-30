package dev.miguelehr.casinoblackjackpc2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.miguelehr.casinoblackjackpc2.network.Card
import dev.miguelehr.casinoblackjackpc2.network.DeckApi
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GameUiState(
    val deckId: String? = null,
    val playerCards: List<Card> = emptyList(),
    val playerScore: Int = 0,
    val machineScore: Int = 0,
    val numCardsToDraw: Int = 2, // entre 2 y 5
    val winnerMessage: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

class BlackjackViewModel : ViewModel() {

    var uiState by mutableStateOf(GameUiState())
        private set

    init {
        // Crear baraja apenas inicia la app
        createNewDeck()
    }

    fun setNumCards(num: Int) {
        val valid = num.coerceIn(2, 5)
        uiState = uiState.copy(numCardsToDraw = valid)
    }

    fun createNewDeck() {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, errorMessage = "", winnerMessage = "")
                val response = DeckApi.service.newShuffledDeck()
                val machineNumber = Random.nextInt(16, 22) // 16–21

                uiState = uiState.copy(
                    deckId = response.deckId,
                    machineScore = machineNumber,
                    playerCards = emptyList(),
                    playerScore = 0,
                    winnerMessage = "",
                    isLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Error creando baraja: ${e.message}"
                )
            }
        }
    }

    fun drawCardsForPlayer() {
        val deckId = uiState.deckId ?: return
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, errorMessage = "", winnerMessage = "")

                val response = DeckApi.service.drawCards(deckId, uiState.numCardsToDraw)
                val cards = response.cards
                val score = calculateScore(cards)
                val machineScore = uiState.machineScore

                val winner = determineWinner(score, machineScore)

                uiState = uiState.copy(
                    playerCards = cards,
                    playerScore = score,
                    winnerMessage = winner,
                    isLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Error obteniendo cartas: ${e.message}"
                )
            }
        }
    }

    // J/Q/K = 10, A = 11, números = valor numérico
    private fun calculateScore(cards: List<Card>): Int {
        var total = 0
        for (card in cards) {
            total += when (card.value) {
                "JACK", "QUEEN", "KING" -> 10
                "ACE" -> 11
                else -> card.value.toIntOrNull() ?: 0
            }
        }
        return total
    }

    private fun determineWinner(player: Int, machine: Int): String {
        val target = 21

        val playerDiff = if (player > target) Int.MAX_VALUE else target - player
        val machineDiff = if (machine > target) Int.MAX_VALUE else target - machine

        return when {
            playerDiff < machineDiff -> "¡Gana el jugador!"
            machineDiff < playerDiff -> "Gana la máquina."
            playerDiff == machineDiff && playerDiff != Int.MAX_VALUE -> "Empate."
            else -> "Ambos se pasaron."
        }
    }
}
package dev.miguelehr.casinoblackjackpc2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.miguelehr.casinoblackjackpc2.network.Card
import dev.miguelehr.casinoblackjackpc2.ui.theme.CasinoBlackjackPC2Theme
import androidx.compose.foundation.layout.statusBarsPadding

class MainActivity : ComponentActivity() {

    // Conectamos el ViewModel
    private val viewModel: BlackjackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CasinoBlackjackPC2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BlackjackScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BlackjackScreen(viewModel: BlackjackViewModel) {
    // Leemos el estado actual del ViewModel
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Casino – Mini Blackjack",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(text = "Baraja ID: ${uiState.deckId ?: "Creando..."}")

        Spacer(Modifier.height(16.dp))

        // Selector de cuántas cartas
        Text(text = "¿Cuántas cartas quieres tomar? (2–5)")
        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(onClick = { viewModel.setNumCards(uiState.numCardsToDraw - 1) }) {
                Text(text = "-")
            }

            Text(
                text = uiState.numCardsToDraw.toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedButton(onClick = { viewModel.setNumCards(uiState.numCardsToDraw + 1) }) {
                Text(text = "+")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botones principales
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.createNewDeck() }) {
                Text(text = "Nueva baraja")
            }

            Button(
                onClick = { viewModel.drawCardsForPlayer() },
                enabled = uiState.deckId != null && !uiState.isLoading
            ) {
                Text(text = "Tomar cartas")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Loading
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        // Errores de la API
        if (uiState.errorMessage.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))

        // Mostrar cartas si existen
        if (uiState.playerCards.isNotEmpty()) {

            Spacer(Modifier.height(24.dp))

            // --- TÍTULO GENERAL ---
            Text(
                text = "Resultado de la ronda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // --- SECCIÓN JUGADOR ---
            Text(
                text = "Jugador",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tus cartas:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.playerCards) { card ->
                    CardView(card = card)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(text = "Puntaje jugador: ${uiState.playerScore}")

            Spacer(Modifier.height(24.dp))

            // --- SECCIÓN MÁQUINA ---
            Text(
                text = "Máquina",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Text(text = "Puntaje máquina: ${uiState.machineScore}")

            Spacer(Modifier.height(24.dp))

            // --- MENSAJE FINAL ---
            Text(
                text = uiState.winnerMessage,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CardView(card: Card) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        AsyncImage(
            model = card.image,
            contentDescription = "${card.value} de ${card.suit}",
            modifier = Modifier
                .height(140.dp)
                .fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(text = "${card.value} de ${card.suit}")
    }
}
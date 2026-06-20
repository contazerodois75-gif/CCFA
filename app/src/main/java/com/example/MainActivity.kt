package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AppDatabase
import com.example.data.repository.GameRepository
import com.example.ui.screens.GameWorkspaceScreen
import com.example.ui.screens.GamesListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.GameViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Room Database and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = GameRepository(database.gameDao)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: GameViewModel = viewModel(
                        factory = GameViewModelFactory(repository)
                    )
                    
                    val currentScreen by viewModel.currentScreen.collectAsState()

                    when (currentScreen) {
                        GameViewModel.Screen.GamesList -> {
                            GamesListScreen(
                                viewModel = viewModel,
                                onGameSelected = { gameId ->
                                    viewModel.selectGame(gameId)
                                }
                            )
                        }
                        GameViewModel.Screen.GameWorkspace -> {
                            GameWorkspaceScreen(
                                viewModel = viewModel,
                                onBack = {
                                    viewModel.navigateTo(GameViewModel.Screen.GamesList)
                                }
                            )
                        }
                        GameViewModel.Screen.CreateGame -> {
                            // Handled by inline Dialog in GamesListScreen for single-screen coherence,
                            // but fallback is simple redirection
                            viewModel.navigateTo(GameViewModel.Screen.GamesList)
                        }
                    }
                }
            }
        }
    }
}

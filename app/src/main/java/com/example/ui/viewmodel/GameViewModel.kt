package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.db.AppDatabase
import com.example.data.model.Game
import com.example.data.model.Player
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // All games list Flow
    val allGames: StateFlow<List<Game>> = repository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation and screen management
    private val _currentScreen = MutableStateFlow<Screen>(Screen.GamesList)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Selected Game Workspace State
    private val _selectedGameId = MutableStateFlow<Int?>(null)
    val selectedGameId: StateFlow<Int?> = _selectedGameId.asStateFlow()

    val selectedGame: StateFlow<Game?> = _selectedGameId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getGameByIdFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedGamePlayers: StateFlow<List<Player>> = _selectedGameId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getPlayersForGame(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Tab in Workspace (0: Geral, 1: Presença, 2: Divisão de Times, 3: Rateio)
    private val _activeWorkspaceTab = MutableStateFlow(0)
    val activeWorkspaceTab: StateFlow<Int> = _activeWorkspaceTab.asStateFlow()

    // Divided teams state cache (so we don't re-calculate on every state change unless desired)
    private val _dividedTeams = MutableStateFlow<Map<String, List<Player>>>(emptyMap())
    val dividedTeams: StateFlow<Map<String, List<Player>>> = _dividedTeams.asStateFlow()

    // Loading states
    private val _isGeminiLoading = MutableStateFlow(false)
    val isGeminiLoading: StateFlow<Boolean> = _isGeminiLoading.asStateFlow()

    sealed interface Screen {
        object GamesList : Screen
        object CreateGame : Screen
        object GameWorkspace : Screen
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectGame(gameId: Int) {
        _selectedGameId.value = gameId
        _activeWorkspaceTab.value = 0
        _dividedTeams.value = emptyMap() // Reset teams until division is ran
        navigateTo(Screen.GameWorkspace)
    }

    fun setWorkspaceTab(tab: Int) {
        _activeWorkspaceTab.value = tab
    }

    // --- Database Operations ---

    fun createGame(title: String, type: String, date: String, playersPerTeam: Int, notes: String) {
        viewModelScope.launch {
            val game = Game(
                title = title.ifEmpty { "Pelada de $type" },
                type = type,
                date = date.ifEmpty { "Sem data" },
                playersPerTeam = playersPerTeam,
                notes = notes
            )
            val newId = repository.insertGame(game)
            selectGame(newId.toInt())
        }
    }

    fun updateGame(game: Game) {
        viewModelScope.launch {
            repository.updateGame(game)
        }
    }

    fun deleteGame(game: Game) {
        viewModelScope.launch {
            repository.deleteGame(game)
            _selectedGameId.value = null
            navigateTo(Screen.GamesList)
        }
    }

    fun addPlayer(name: String, attendance: String, skillLevel: Int, position: String, staminaLevel: Int, ageCategory: String) {
        val gameId = _selectedGameId.value ?: return
        viewModelScope.launch {
            val player = Player(
                gameId = gameId,
                name = name.trim().ifEmpty { "Jogador" },
                attendance = attendance,
                skillLevel = skillLevel.coerceIn(1, 5),
                position = position.ifEmpty { "-" },
                staminaLevel = staminaLevel.coerceIn(1, 5),
                ageCategory = ageCategory
            )
            repository.insertPlayer(player)
        }
    }

    fun addPlayersBulk(namesRaw: String) {
        val gameId = _selectedGameId.value ?: return
        viewModelScope.launch {
            val names = namesRaw.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            val playersToInsert = names.map { name ->
                val cleanedName = name.replace(Regex("^([\\d\\.\\-\\x20\\*]+)"), "").trim()
                Player(
                    gameId = gameId,
                    name = cleanedName.ifEmpty { "Jogador" },
                    attendance = "Confirmado",
                    skillLevel = 3,
                    position = "-",
                    staminaLevel = 3,
                    ageCategory = "Livre"
                )
            }
            if (playersToInsert.isNotEmpty()) {
                repository.insertPlayers(playersToInsert)
            }
        }
    }

    fun updatePlayer(player: Player) {
        viewModelScope.launch {
            repository.updatePlayer(player)
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            repository.deletePlayer(player)
        }
    }

    // --- Team Balancing Logic ---

    private val _balanceCriteria = MutableStateFlow("tecnico")
    val balanceCriteria: StateFlow<String> = _balanceCriteria.asStateFlow()

    fun balanceTeams(criteria: String = _balanceCriteria.value) {
        _balanceCriteria.value = criteria
        val players = selectedGamePlayers.value.filter { it.attendance == "Confirmado" }
        val game = selectedGame.value ?: return
        val playersPerTeam = game.playersPerTeam

        if (players.size < 2) {
            _dividedTeams.value = emptyMap()
            return
        }

        // Divide players according to selected technical or random criteria
        val sortedPlayers = when (criteria) {
            "aleatorio" -> {
                // Sorteio Aleatório / Pure luck lottery
                players.shuffled()
            }
            "fisico" -> {
                // Balanced strict by stamina/fôlego/speed
                players.sortedByDescending { it.staminaLevel }
            }
            "misto" -> {
                // Mix skill + stamina level combined score
                players.sortedByDescending { (it.skillLevel * 1.5) + it.staminaLevel }
            }
            "posicoes" -> {
                // Group by similar positions to separate them evenly, then distribute within each position
                val keepers = players.filter { it.position.contains("Gol", ignoreCase = true) || it.position.contains("Goleiro", ignoreCase = true) }
                    .sortedByDescending { it.skillLevel }
                val defenders = players.filter { 
                    it.position.contains("Def", ignoreCase = true) || 
                    it.position.contains("Zag", ignoreCase = true) || 
                    it.position.contains("Lib", ignoreCase = true) 
                }.sortedByDescending { it.skillLevel }
                
                val midfields = players.filter { 
                    it.position.contains("Mei", ignoreCase = true) || 
                    it.position.contains("Ala", ignoreCase = true) || 
                    it.position.contains("Lev", ignoreCase = true) 
                }.sortedByDescending { it.skillLevel }
                
                val forwards = players.filter { 
                    it.position.contains("Ata", ignoreCase = true) || 
                    it.position.contains("Piv", ignoreCase = true) || 
                    it.position.contains("Opo", ignoreCase = true) 
                }.sortedByDescending { it.skillLevel }
                
                val others = players.filterNot {
                    keepers.contains(it) || defenders.contains(it) || midfields.contains(it) || forwards.contains(it)
                }.sortedByDescending { it.skillLevel }
                
                keepers + defenders + midfields + forwards + others
            }
            else -> {
                // "tecnico" (Strictly by skillStars)
                players.sortedByDescending { it.skillLevel }
            }
        }

        // Count teams needed
        val teamCount = Math.ceil(players.size.toDouble() / playersPerTeam.toDouble()).toInt().coerceAtLeast(2)

        // Initialize teams
        val teamsMap = LinkedHashMap<String, MutableList<Player>>()
        val coletes = listOf("Colete Verde", "Colete Azul", "Sem Colete", "Colete Amarelo", "Colete Vermelho", "Colete Preto", "Time Branco", "Time Cinza")
        for (i in 0 until teamCount) {
            val teamName = coletes.getOrElse(i) { "Time ${i + 1}" }
            teamsMap[teamName] = mutableListOf()
        }

        // Perform serpentine draft (or simple random distribution if pure random was chosen)
        if (criteria == "aleatorio") {
            var idx = 0
            for (player in sortedPlayers) {
                val teamName = coletes.getOrElse(idx % teamCount) { "Time ${(idx % teamCount) + 1}" }
                teamsMap[teamName]?.add(player)
                idx++
            }
        } else {
            // Serpentine draft to balance teams effectively!
            var goingForward = true
            var currentTeamIdx = 0

            for (player in sortedPlayers) {
                val teamName = coletes.getOrElse(currentTeamIdx) { "Time ${currentTeamIdx + 1}" }
                teamsMap[teamName]?.add(player)

                if (goingForward) {
                    if (currentTeamIdx == teamCount - 1) {
                        goingForward = false
                    } else {
                        currentTeamIdx++
                    }
                } else {
                    if (currentTeamIdx == 0) {
                        goingForward = true
                    } else {
                        currentTeamIdx--
                    }
                }
            }
        }

        _dividedTeams.value = teamsMap
    }

    fun triggerGeminiCoachReview() {
        val game = selectedGame.value ?: return
        val players = selectedGamePlayers.value.filter { it.attendance == "Confirmado" }
        val currentTeams = _dividedTeams.value

        if (players.isEmpty() || currentTeams.isEmpty()) return

        _isGeminiLoading.value = true
        viewModelScope.launch {
            try {
                val reviewText = GeminiClient.generateCoachReview(
                    gameType = game.type,
                    gameTitle = game.title,
                    players = players,
                    teams = currentTeams
                )
                // Cache back into the database
                repository.updateGame(game.copy(geminiReview = reviewText))
            } catch (e: Exception) {
                // If it fails, we keep the existing or display standard message
            } finally {
                _isGeminiLoading.value = false
            }
        }
    }
}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

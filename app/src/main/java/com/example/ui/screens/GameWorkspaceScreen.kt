package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Game
import com.example.data.model.Player
import com.example.ui.theme.BallYellowGold
import com.example.ui.theme.GrassGreenPrimary
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameWorkspaceScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val game by viewModel.selectedGame.collectAsState()
    val players by viewModel.selectedGamePlayers.collectAsState()
    val activeTab by viewModel.activeWorkspaceTab.collectAsState()

    val context = LocalContext.current

    if (game == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentGame = game!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentGame.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentGame.type} • ${currentGame.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.balanceTeams() }) {
                        Icon(Icons.Default.Casino, contentDescription = "Reequilibrar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Workspace primary tabs
            TabRow(
                selectedTabIndex = activeTab.coerceIn(0, 2),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { viewModel.setWorkspaceTab(0) },
                    text = { Text("Geral", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_general")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { viewModel.setWorkspaceTab(1) },
                    text = { Text("Jogadores", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_players")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { viewModel.setWorkspaceTab(2) },
                    text = { Text("Times & Sorteio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_teams")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Panels
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> TabGeneral(game = currentGame, players = players, viewModel = viewModel)
                    1 -> TabPlayers(players = players, viewModel = viewModel)
                    2 -> TabTeams(game = currentGame, players = players, viewModel = viewModel)
                    else -> TabGeneral(game = currentGame, players = players, viewModel = viewModel)
                }
            }
        }
    }
}

// ==================== TABS IMPLEMENTATIONS ====================

@Composable
fun TabGeneral(
    game: Game,
    players: List<Player>,
    viewModel: GameViewModel
) {
    var title by varOf(game.title)
    var date by varOf(game.date)
    var playersPerTeamStr by varOf(game.playersPerTeam.toString())
    var notes by varOf(game.notes)

    val confirmedCount = players.count { it.attendance == "Confirmado" }
    val totalCount = players.size

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Quick statistics highlight card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Resumo do Grupo",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Confirmados", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("$confirmedCount Atletas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column {
                            Text("Cadastrados", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("$totalCount Atletas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column {
                            Text("Tamanho Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("${game.playersPerTeam}x${game.playersPerTeam}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        // Details Editor form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Editar Detalhes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nome da Pelada") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Data e Horário") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = playersPerTeamStr,
                        onValueChange = { playersPerTeamStr = it },
                        label = { Text("Jogadores por Time") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Avisos / Notas extras") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val playersNum = playersPerTeamStr.toIntOrNull() ?: 5
                            viewModel.updateGame(
                                game.copy(
                                    title = title,
                                    date = date,
                                    playersPerTeam = playersNum,
                                    notes = notes
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_game_details_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvar Alterações")
                    }
                }
            }
        }
    }
}

@Composable
fun TabPlayers(
    players: List<Player>,
    viewModel: GameViewModel
) {
    var nameInput by varOf("")
    var ratingInput by varOf(3)
    var staminaInput by varOf(3)
    var ageCategoryInput by varOf("Livre")
    var positionInput by varOf("")

    // WhatsApp paste input
    var showBulkInput by varOf(false)
    var bulkPasteText by varOf("")

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Quick add player panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cadastrar Aluno / Jogador", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = { showBulkInput = !showBulkInput }) {
                            Text(if (showBulkInput) "Add Individual" else "Importar Lista Whats")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (showBulkInput) {
                        // Bulk imports TEXTAREA
                        Text(
                            "Cole os nomes copiados do grupo de WhatsApp (um por linha):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = bulkPasteText,
                            onValueChange = { bulkPasteText = it },
                            placeholder = { Text("Ex:\n1. Marquinhos\n2. Lucas\n3. Juninho") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("bulk_players_textarea"),
                            maxLines = 10
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (bulkPasteText.trim().isNotEmpty()) {
                                    viewModel.addPlayersBulk(bulkPasteText)
                                    bulkPasteText = ""
                                    showBulkInput = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar Importação de Lista")
                        }
                    } else {
                        // Single add
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nome do Atleta") },
                            placeholder = { Text("Ex: Carlos Silva") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("player_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Technical Rating
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Nível Técnico (⭐)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Row(modifier = Modifier.padding(top = 4.dp)) {
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = if (i <= ratingInput) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = if (i <= ratingInput) BallYellowGold else Color.Gray,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable { ratingInput = i }
                                        )
                                    }
                                }
                            }

                            // Physical Stamina Rating
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fôlego / Corrida (⚡)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Row(modifier = Modifier.padding(top = 4.dp)) {
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = if (i <= staminaInput) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = if (i <= staminaInput) Color(0xFF6366F1) else Color.Gray,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable { staminaInput = i }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Position
                            OutlinedTextField(
                                value = positionInput,
                                onValueChange = { positionInput = it },
                                label = { Text("Posição") },
                                placeholder = { Text("Ex: Goleiro, Zagueiro, Ala") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("player_position_input"),
                                singleLine = true
                            )

                            // Age categories chips
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text("Idade / Senioridade", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Livre", "Veterano", "Juvenil").forEach { cat ->
                                        FilterChip(
                                            selected = ageCategoryInput == cat,
                                            onClick = { ageCategoryInput = cat },
                                            label = { Text(cat, fontSize = 10.sp) },
                                            modifier = Modifier.height(32.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (nameInput.isNotBlank()) {
                                    viewModel.addPlayer(
                                        name = nameInput,
                                        attendance = "Confirmado",
                                        skillLevel = ratingInput,
                                        position = positionInput,
                                        staminaLevel = staminaInput,
                                        ageCategory = ageCategoryInput
                                    )
                                    nameInput = ""
                                    positionInput = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_player_submit_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Adicionar à Pelada")
                        }
                    }
                }
            }
        }

        // List Header
        item {
            Text(
                text = "Lista Geral (${players.size} Cadastrados)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (players.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Nenhum jogador na lista. Adicione-os acima ou copie o WhatsApp!", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(players) { player ->
                PlayerRowItem(
                    player = player,
                    onUpdate = { updatedPlayer -> viewModel.updatePlayer(updatedPlayer) },
                    onDelete = { viewModel.deletePlayer(player) }
                )
            }
        }
    }
}

@Composable
fun PlayerRowItem(
    player: Player,
    onUpdate: (Player) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (player.attendance == "Confirmado")
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (player.attendance == "Confirmado")
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else
                Color.LightGray.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (player.attendance == "Confirmado")
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    if (player.position.isNotBlank() && player.position != "-") {
                        Spacer(modifier = Modifier.width(6.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(player.position, fontSize = 10.sp) },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Star rating display
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= player.skillLevel) BallYellowGold else Color.LightGray,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onUpdate(player.copy(skillLevel = i)) }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Folego: ${player.staminaLevel}⚡ • ${player.ageCategory}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick toggle attendance
            val isConfirmed = player.attendance == "Confirmado"
            FilterChip(
                selected = isConfirmed,
                onClick = {
                    onUpdate(player.copy(attendance = if (isConfirmed) "Dispensa" else "Confirmado"))
                },
                label = { Text(if (isConfirmed) "Confirmado" else "Dispensa", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.padding(end = 6.dp)
            )

            // Delete
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Deletar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun TabTeams(
    game: Game,
    players: List<Player>,
    viewModel: GameViewModel
) {
    val dividedTeams by viewModel.dividedTeams.collectAsState()
    val isGeminiLoading by viewModel.isGeminiLoading.collectAsState()
    val activeCriteria by viewModel.balanceCriteria.collectAsState()

    val confirmedPlayers = players.filter { it.attendance == "Confirmado" }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Balancer & Raffle card selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Configurar Escalação & Sorteio", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Escolha a categoria técnica ou opte por sorteio aleatório puro (loteria) para montar suas equipes:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val options = listOf(
                        Triple("tecnico", "Equilíbrio Técnico ⭐", "Draft por qualidade técnica"),
                        Triple("fisico", "Equilíbrio Físico ⚡", "Draft por fôlego/velocidade"),
                        Triple("posicoes", "Draft de Posições 📋", "Evita concentrar goleiros/defensores"),
                        Triple("aleatorio", "Sorteio Aleatório 🎲", "Loteria pura, 100% sorte")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        options.forEach { (key, label, desc) ->
                            val isSelected = activeCriteria == key
                            Surface(
                                onClick = { viewModel.balanceTeams(key) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.balanceTeams(key) }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.balanceTeams() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("balance_teams_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (activeCriteria == "aleatorio") "Sortear Equipes Agora!" else "Escalar Equipes Balanceadas!")
                    }
                }
            }
        }

        // Teams display
        if (dividedTeams.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Times não montados ainda!",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Atletas confirmados ativos: ${confirmedPlayers.size}. Clique no botão acima para escalá-los em times.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Cards for each team
            items(dividedTeams.keys.toList()) { teamName ->
                val teamPlayers = dividedTeams[teamName] ?: emptyList()
                val skillAvg = if (teamPlayers.isNotEmpty()) teamPlayers.map { it.skillLevel }.average() else 0.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        // Team header
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = teamName,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 16.sp
                            )
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text("Média: ${String.format("%.1f", skillAvg)}⭐", fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                            }
                        }

                        // Players
                        Column(modifier = Modifier.padding(12.dp)) {
                            teamPlayers.forEachIndexed { idx, player ->
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${idx+1}. ${player.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (player.position.isNotBlank() && player.position != "-") {
                                            Text(player.position, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                                        }
                                        Text("${player.skillLevel}⭐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (idx < teamPlayers.size - 1) {
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }

            // Coach review triggering section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tática & Resenha do Treinador AI 🤖",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Gere análises táticas furadas, previsões de placar e comentários divertidos baseados na qualidade do seu time, perfeitos para encaminhar para o WhatsApp do grupo!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isGeminiLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("O treinador está pensando nas asneiras táticas...", fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.triggerGeminiCoachReview() },
                                modifier = Modifier.fillMaxWidth().testTag("coach_api_call_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (game.geminiReview != null) "Regerar Comentário" else "Análisar Time com AI")
                            }
                        }

                        // Display existing cached review description
                        game.geminiReview?.let { review ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color.Green, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Professor AI diz:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = review,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    // Clipboard Copy button
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val clipboard = LocalClipboardManager.current
                                    val context = LocalContext.current
                                    TextButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(review))
                                            Toast.makeText(context, "Resenha copiada para o WhatsApp!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copiar Resenha", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

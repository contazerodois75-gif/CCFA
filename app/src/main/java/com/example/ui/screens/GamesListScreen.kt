package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Game
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesListScreen(
    viewModel: GameViewModel,
    onGameSelected: (Int) -> Unit
) {
    val games by viewModel.allGames.collectAsState()
    var showCreateDialog by varOf(false)
    var showDeleteConfirmDialog by varOf<Game?>(null)

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(vertical = 24.dp, horizontal = 20.dp)
            ) {
                // Background Tactical Pitch lines
                Canvas(
                    modifier = Modifier.matchParentSize()
                ) {
                    val lineColor = Color.White.copy(alpha = 0.08f)
                    val strokeWidth = 2.dp.toPx()
                    
                    // Draw pitch outer border
                    drawRect(
                        color = lineColor,
                        topLeft = Offset(0f, 0f),
                        size = size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )
                    
                    // Draw middle line
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = strokeWidth
                    )
                    
                    // Draw center circle
                    drawCircle(
                        color = lineColor,
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width / 2, size.height / 2),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )
                    
                    // Draw penalty area at bottom
                    drawRect(
                        color = lineColor,
                        topLeft = Offset(size.width * 0.15f, size.height * 0.85f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.15f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )

                    // Draw penalty area at top
                    drawRect(
                        color = lineColor,
                        topLeft = Offset(size.width * 0.15f, 0f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.15f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Logo Crest Shield
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "CCFA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "CCFA ORGANIZADORES",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gestão de Peladas & Copas Amadoras",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Stats pill on the top-right
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sports,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${games.size} Jogos",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Novo Jogo / Copa") },
                text = { Text("Novo Jogo / Copa") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("nova_pelada_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (games.isEmpty()) {
                EmptyGamesState(onCreateClick = { showCreateDialog = true })
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "Jogos e Copas CCFA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(games) { game ->
                        GameItemCard(
                            game = game,
                            onClick = { onGameSelected(game.id) },
                            onDeleteClick = { showDeleteConfirmDialog = game }
                        )
                    }
                }
            }
        }
    }

    // CREATE GAME DIALOG
    if (showCreateDialog) {
        CreateGameDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, type, date, playersPerTeam, notes ->
                viewModel.createGame(title, type, date, playersPerTeam, notes)
                showCreateDialog = false
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    showDeleteConfirmDialog?.let { gameToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Excluir Jogo/Copa?") },
            text = { Text("Isso apagará permanentemente o jogo '${gameToDelete.title}' e todos os atletas cadastrados nele.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGame(gameToDelete)
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun GameItemCard(
    game: Game,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_item_card_${game.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sport Icon Circular frame
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getSportIcon(game.type),
                        contentDescription = game.type,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Game Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Data",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = game.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Times: ${game.playersPerTeam}x${game.playersPerTeam}") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Esporte: ${game.type}") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun EmptyGamesState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val boardColor = Color(0xFF1E3A1E) // Dark tactical soccer board green
                val lineColor = Color.White.copy(alpha = 0.5f)
                val strokeW = 1.5.dp.toPx()
                
                // Draw Board
                drawRoundRect(
                    color = boardColor,
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
                
                // Draw Pitch outer line
                val pad = 10.dp.toPx()
                drawRect(
                    color = lineColor,
                    topLeft = Offset(pad, pad),
                    size = androidx.compose.ui.geometry.Size(size.width - 2 * pad, size.height - 2 * pad),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                )
                
                // Draw middle line
                val midX = size.width / 2
                drawLine(
                    color = lineColor,
                    start = Offset(midX, pad),
                    end = Offset(midX, size.height - pad),
                    strokeWidth = strokeW
                )
                
                // Draw center circle
                drawCircle(
                    color = lineColor,
                    radius = (size.height - 2 * pad) * 0.2f,
                    center = Offset(midX, size.height / 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                )
                
                // Draw player dots
                // X players (Team A in primary color)
                drawCircle(
                    color = Color(0xFF6366F1), // Bright Indigo
                    radius = 5.dp.toPx(),
                    center = Offset(midX * 0.5f, size.height * 0.4f)
                )
                drawCircle(
                    color = Color(0xFF6366F1),
                    radius = 5.dp.toPx(),
                    center = Offset(midX * 0.6f, size.height * 0.7f)
                )
                
                // O players (Team B in Gold)
                drawCircle(
                    color = Color(0xFFF59E0B), // Golden accent
                    radius = 5.dp.toPx(),
                    center = Offset(midX * 1.4f, size.height * 0.35f)
                )
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = 5.dp.toPx(),
                    center = Offset(midX * 1.5f, size.height * 0.65f)
                )
                
                // Ball
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(midX * 1.05f, size.height * 0.52f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pronto para Organizar!",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Crie seus jogos no CCFA ORGANIZADORES. Cadastre seus atletas, defina fôlego, fardamento, médias técnicas e escale equipes taticamente equilibradas com facilidade.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onCreateClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Criar Seu Primeiro Jogo")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, String) -> Unit
) {
    var title by varOf("")
    var selectedType by varOf("Futebol")
    var date by varOf("")
    var playersPerTeamStr by varOf("5")
    var notes by varOf("")

    val sports = listOf("Futebol", "Vôlei", "Basquete", "Outro")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Novo Jogo / Copa CCFA") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nome do Jogo / Copa") },
                        placeholder = { Text("Ex: Copa CCFA de Sábado, Campeonato Amador") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_game_title_input"),
                        singleLine = true
                    )
                }
                item {
                    Text("Esporte", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        sports.forEach { sport ->
                            InputChip(
                                selected = selectedType == sport,
                                onClick = { selectedType = sport },
                                label = { Text(sport) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Data e Horário") },
                        placeholder = { Text("Ex: Sábado, 16:00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = playersPerTeamStr,
                        onValueChange = { playersPerTeamStr = it },
                        label = { Text("Jogadores por Time") },
                        placeholder = { Text("Ex: 5, 6, 11") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Avisos / Notas extras") },
                        placeholder = { Text("Ex: Trazer chuteira society, colete...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val playersPerTeam = playersPerTeamStr.toIntOrNull() ?: 5
                    onConfirm(title, selectedType, date, playersPerTeam, notes)
                },
                modifier = Modifier.testTag("create_game_submit_button")
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun getSportIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        "Futebol" -> Icons.Default.SportsSoccer
        "Vôlei" -> Icons.Default.SportsVolleyball
        "Basquete" -> Icons.Default.SportsBasketball
        else -> Icons.Default.Sports
    }
}

// Utility companion to make states shorter
@Composable
fun <T> varOf(initialValue: T) = remember { mutableStateOf(initialValue) }

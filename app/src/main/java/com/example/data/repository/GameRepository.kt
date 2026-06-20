package com.example.data.repository

import com.example.data.db.GameDao
import com.example.data.model.Game
import com.example.data.model.Player
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val allGames: Flow<List<Game>> = gameDao.getAllGames()

    fun getGameByIdFlow(id: Int): Flow<Game?> = gameDao.getGameByIdFlow(id)

    suspend fun getGameById(id: Int): Game? = gameDao.getGameById(id)

    suspend fun insertGame(game: Game): Long = gameDao.insertGame(game)

    suspend fun updateGame(game: Game) = gameDao.updateGame(game)

    suspend fun deleteGame(game: Game) = gameDao.deleteGame(game)

    fun getPlayersForGame(gameId: Int): Flow<List<Player>> = gameDao.getPlayersForGame(gameId)

    suspend fun insertPlayer(player: Player): Long = gameDao.insertPlayer(player)

    suspend fun updatePlayer(player: Player) = gameDao.updatePlayer(player)

    suspend fun deletePlayer(player: Player) = gameDao.deletePlayer(player)

    suspend fun deletePlayerById(id: Int) = gameDao.deletePlayerById(id)

    suspend fun insertPlayers(players: List<Player>) = gameDao.insertPlayers(players)
}

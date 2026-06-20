package com.example.data.db

import androidx.room.*
import com.example.data.model.Game
import com.example.data.model.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // Game Operations
    @Query("SELECT * FROM games ORDER BY timestamp DESC")
    fun getAllGames(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun getGameByIdFlow(id: Int): Flow<Game?>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Int): Game?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game): Long

    @Update
    suspend fun updateGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)

    // Player Operations
    @Query("SELECT * FROM players WHERE gameId = :gameId ORDER BY name ASC")
    fun getPlayersForGame(gameId: Int): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player): Long

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deletePlayerById(playerId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<Player>)
}

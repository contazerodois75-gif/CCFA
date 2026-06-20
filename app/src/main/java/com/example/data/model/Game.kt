package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "Futebol", "Vôlei", "Basquete", "Outro"
    val date: String, // e.g., "Sábado, 16:00"
    val playersPerTeam: Int = 5,
    val notes: String = "",
    val geminiReview: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["gameId"])]
)
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameId: Int,
    val name: String,
    val attendance: String = "Confirmado", // "Confirmado", "Dispensa"
    val skillLevel: Int = 3, // 1 to 5 stars
    val position: String = "-", // Goleiro, Defensor, Meio, Atacante
    val staminaLevel: Int = 3, // 1 to 5 stars (Physical / speed / fôlego)
    val ageCategory: String = "Livre" // "Livre", "Veterano", "Juvenil"
)


package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Player
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateCoachReview(
        gameType: String,
        gameTitle: String,
        players: List<Player>,
        teams: Map<String, List<Player>>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", ignoreCase = true)) {
            Log.w(TAG, "Gemini API Key is missing or default. Graceful fallback activated.")
            return "⚠️ Chave API do Gemini não configurada! Adicione sua chave GEMINI_API_KEY no painel de segredos para receber comentários humorísticos do Treinador Inteligente. Mas relaxa, a divisão dos times já está pronta e equilibrada de forma justa!"
        }

        val playersListBuilder = StringBuilder()
        players.forEach { p ->
            playersListBuilder.append("- ${p.name} (Técnico: ${p.skillLevel}⭐, Fôlego: ${p.staminaLevel}⚡, Idade: ${p.ageCategory}, Posição: ${p.position})\n")
        }

        val teamsListBuilder = StringBuilder()
        teams.forEach { (teamName, teamPlayers) ->
            teamsListBuilder.append("$teamName:\n")
            teamPlayers.forEach { p ->
                teamsListBuilder.append("  * ${p.name} (Técnico: ${p.skillLevel}⭐, Fôlego: ${p.staminaLevel}⚡, Idade: ${p.ageCategory}, Posição: ${p.position})\n")
            }
        }

        val prompt = """
            Você é o narrador e técnico bem humorado (um verdadeiro 'tchelo' ou 'professor') de um grupo de pelada amadora chamado '$gameTitle' de modalidade $gameType.
            Aqui está a lista acumulada dos jogadores confirmados:
            ${playersListBuilder.toString()}
            
            Eles foram divididos nos seguintes times de forma equilibrada:
            ${teamsListBuilder.toString()}
            
            Gere uma análise tática bem-humorada, descontraída e motivacional em português brasileiro sobre essa divisão:
            1. Analise os times e faça piada leve (saudável) sobre quem é o 'craque' (as estrelas mais altas) e quem terá que segurar a onda ('perna de pau' das estrelas mais baixas).
            2. Proponha táticas engraçadas baseadas no esporte (ex: no futebol 'chuta pra onde tá virado', no vôlei 'não deixa cair e reza', no basquete 'bota a bola debaixo do braço e corre das faltas').
            3. Dê uma previsão maluca do placar final dessa disputa e declare um palpite para o vencedor.
            4. Use gírias locais e termos comuns de peladas brasileiras (ex: canelada, fominha, colete, café com leite, artilheiro).
            
            Escreva um texto contínuo, vibrante, divertido e amigável (cerca de 150 a 200 palavras). Termine com um grito de guerra ou frase de efeito.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "Você é um treinador de pelada brasileiro fanfarrão, que fala de forma descontraída com amigos de esporte amador no WhatsApp."))
            )
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Tivemos um silêncio técnico no vestiário! Os times já estão prontos, agora é só colocar as chuteiras e ir pro jogo."
        } catch (e: Exception) {
            Log.e(TAG, "Error generating coach commentary", e)
            "O vestiário está muito barulhento agora (erro de conexão com o Gemini)! Mas os times foram escalados com perfeição localmente. Vamos pro jogo!"
        }
    }
}

package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.engine.LudoAi
import com.example.engine.LudoPathManager
import com.example.engine.LudoRules
import com.example.model.GameLogEntry
import com.example.model.GameMode
import com.example.model.LudoGameState
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.PlayerType
import com.example.model.Token
import com.example.model.TokenState
import com.example.model.TurnPhase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    val soundManager = SoundManager()
    private val prefs = application.getSharedPreferences("ludo_game_prefs", Context.MODE_PRIVATE)

    private val _gameState = MutableStateFlow(LudoGameState())
    val gameState: StateFlow<LudoGameState> = _gameState.asStateFlow()

    // For token movement step-by-step animation
    private val _animatingToken = MutableStateFlow<Token?>(null)
    val animatingToken: StateFlow<Token?> = _animatingToken.asStateFlow()

    private val _animatingStep = MutableStateFlow<Int>(-1)
    val animatingStep: StateFlow<Int> = _animatingStep.asStateFlow()

    private var aiTurnJob: Job? = null
    private var isDiceRollingAnimation: Boolean = false

    init {
        // Load saved state or initialize a fresh game
        val restored = restoreSavedGame()
        if (!restored) {
            initGame(GameMode.FOUR_PLAYER)
        }
    }

    fun initGame(mode: GameMode, aiCount: Int = 3) {
        aiTurnJob?.cancel()
        _animatingToken.value = null
        _animatingStep.value = -1

        val players = createPlayersForMode(mode, aiCount)
        _gameState.value = LudoGameState(
            gameMode = mode,
            players = players,
            activePlayerIndex = 0,
            currentDiceValue = 6,
            isDiceRolled = false,
            turnPhase = TurnPhase.WAITING_FOR_ROLL,
            eligibleTokenIds = emptySet(),
            consecutiveSixes = 0,
            winner = null,
            finishRankings = emptyList(),
            logs = listOf(
                GameLogEntry(
                    text = "Welcome to Classic Ludo! ${players[0].name}'s turn to roll.",
                    playerColor = players[0].color
                )
            ),
            bonusRollAwarded = false,
            soundEnabled = soundManager.isSoundEnabled,
            computerAiCount = aiCount
        )

        saveGame()
        checkAiTurn()
    }

    private fun createPlayersForMode(mode: GameMode, aiCount: Int): List<Player> {
        return when (mode) {
            GameMode.TWO_PLAYER -> listOf(
                Player(
                    color = PlayerColor.RED,
                    name = "Player 1 (Red)",
                    type = PlayerType.HUMAN
                ),
                Player(
                    color = PlayerColor.YELLOW,
                    name = "Player 2 (Yellow)",
                    type = PlayerType.HUMAN
                )
            )
            GameMode.THREE_PLAYER -> listOf(
                Player(
                    color = PlayerColor.RED,
                    name = "Player 1 (Red)",
                    type = PlayerType.HUMAN
                ),
                Player(
                    color = PlayerColor.GREEN,
                    name = "Player 2 (Green)",
                    type = PlayerType.HUMAN
                ),
                Player(
                    color = PlayerColor.YELLOW,
                    name = "Player 3 (Yellow)",
                    type = PlayerType.HUMAN
                )
            )
            GameMode.FOUR_PLAYER -> listOf(
                Player(
                    color = PlayerColor.RED,
                    name = "Player 1 (Red)",
                    type = PlayerType.HUMAN
                ),
                Player(
                    color = PlayerColor.GREEN,
                    name = "Player 2 (Green)",
                    type = PlayerType.HUMAN
                ),
                Player(
                    color = PlayerColor.YELLOW,
                    name = "Player 3 (Yellow)",
                    type = PlayerType.HUMAN
                ),
                Player(
                    color = PlayerColor.BLUE,
                    name = "Player 4 (Blue)",
                    type = PlayerType.HUMAN
                )
            )
            GameMode.VS_COMPUTER -> {
                when (aiCount) {
                    1 -> listOf(
                        Player(
                            color = PlayerColor.RED,
                            name = "You (Red)",
                            type = PlayerType.HUMAN
                        ),
                        Player(
                            color = PlayerColor.YELLOW,
                            name = "Bot Yellow",
                            type = PlayerType.COMPUTER
                        )
                    )
                    2 -> listOf(
                        Player(
                            color = PlayerColor.RED,
                            name = "You (Red)",
                            type = PlayerType.HUMAN
                        ),
                        Player(
                            color = PlayerColor.GREEN,
                            name = "Bot Green",
                            type = PlayerType.COMPUTER
                        ),
                        Player(
                            color = PlayerColor.YELLOW,
                            name = "Bot Yellow",
                            type = PlayerType.COMPUTER
                        )
                    )
                    else -> listOf(
                        Player(
                            color = PlayerColor.RED,
                            name = "You (Red)",
                            type = PlayerType.HUMAN
                        ),
                        Player(
                            color = PlayerColor.GREEN,
                            name = "Bot Green",
                            type = PlayerType.COMPUTER
                        ),
                        Player(
                            color = PlayerColor.YELLOW,
                            name = "Bot Yellow",
                            type = PlayerType.COMPUTER
                        ),
                        Player(
                            color = PlayerColor.BLUE,
                            name = "Bot Blue",
                            type = PlayerType.COMPUTER
                        )
                    )
                }
            }
        }
    }

    /**
     * Trigger a dice roll.
     */
    fun rollDice() {
        val state = _gameState.value
        if (state.turnPhase != TurnPhase.WAITING_FOR_ROLL || isDiceRollingAnimation) {
            return
        }

        val activePlayer = state.activePlayer ?: return
        if (activePlayer.hasWon) {
            advanceToNextTurn(bonus = false)
            return
        }

        viewModelScope.launch {
            isDiceRollingAnimation = true
            _gameState.update { it.copy(turnPhase = TurnPhase.ROLLING_DICE) }
            soundManager.playDiceRoll()

            // Rapid dice face cycling animation
            val rollSteps = 7
            for (i in 0 until rollSteps) {
                val tempDice = Random.nextInt(1, 7)
                _gameState.update { it.copy(currentDiceValue = tempDice) }
                delay(40L + (i * 12L))
            }

            // Final rolled dice value
            val finalDice = Random.nextInt(1, 7)
            val newConsecutiveSixes = if (finalDice == 6) state.consecutiveSixes + 1 else 0

            _gameState.update {
                it.copy(
                    currentDiceValue = finalDice,
                    isDiceRolled = true,
                    consecutiveSixes = newConsecutiveSixes
                )
            }

            isDiceRollingAnimation = false

            // Rule: 3 consecutive sixes in a row loses turn
            if (newConsecutiveSixes >= 3) {
                addLog("${activePlayer.name} rolled three 6s in a row! Turn skipped.", activePlayer.color)
                delay(800)
                advanceToNextTurn(bonus = false)
                return@launch
            }

            addLog("${activePlayer.name} rolled a $finalDice!", activePlayer.color)

            // Calculate eligible tokens
            val eligibleTokens = LudoRules.getEligibleTokens(activePlayer, finalDice)

            if (eligibleTokens.isEmpty()) {
                addLog("No legal moves available for ${activePlayer.name}.", activePlayer.color)
                _gameState.update { it.copy(turnPhase = TurnPhase.ROUND_TRANSITION, eligibleTokenIds = emptySet()) }
                val delayTime = if (state.fastAiSpeed) 400L else 750L
                delay(delayTime)
                advanceToNextTurn(bonus = false)
            } else {
                _gameState.update {
                    it.copy(
                        turnPhase = TurnPhase.SELECTING_TOKEN,
                        eligibleTokenIds = eligibleTokens
                    )
                }

                // If only 1 move is possible and player is Computer (or auto-move)
                if (activePlayer.type == PlayerType.COMPUTER) {
                    val delayTime = if (state.fastAiSpeed) 300L else 650L
                    delay(delayTime)
                    val chosenTokenId = LudoAi.chooseBestTokenToMove(
                        activePlayer = activePlayer,
                        allPlayers = state.players,
                        diceValue = finalDice,
                        eligibleTokenIds = eligibleTokens
                    )
                    if (chosenTokenId != null) {
                        moveToken(chosenTokenId)
                    }
                } else if (eligibleTokens.size == 1) {
                    // For human with only 1 possible move, highlight and let them click or auto-move after brief delay
                    // Providing tactile tap feels better, but we also support direct tap
                }
            }
        }
    }

    /**
     * User or AI selected a token to move.
     */
    fun selectToken(tokenId: Int) {
        val state = _gameState.value
        val activePlayer = state.activePlayer ?: return

        if (state.turnPhase != TurnPhase.SELECTING_TOKEN) return
        if (tokenId !in state.eligibleTokenIds) return

        moveToken(tokenId)
    }

    private fun moveToken(tokenId: Int) {
        val state = _gameState.value
        val activePlayer = state.activePlayer ?: return
        val token = activePlayer.tokens.find { it.id == tokenId } ?: return
        val dice = state.currentDiceValue

        viewModelScope.launch {
            _gameState.update {
                it.copy(
                    turnPhase = TurnPhase.ANIMATING_MOVE,
                    eligibleTokenIds = emptySet()
                )
            }

            val startStep = token.stepCount
            val targetStep = LudoRules.getTargetStepCount(token, dice)

            // Step by step animation
            _animatingToken.value = token

            if (startStep == -1) {
                // Token exiting yard
                soundManager.playTokenStep(0)
                _animatingStep.value = 0
                delay(120)
            } else {
                for (s in (startStep + 1)..targetStep) {
                    _animatingStep.value = s
                    soundManager.playTokenStep(s)
                    val stepDelay = if (state.fastAiSpeed) 70L else 110L
                    delay(stepDelay)
                }
            }

            _animatingToken.value = null
            _animatingStep.value = -1

            // Apply move to state
            val updatedToken = token.copy(stepCount = targetStep)
            val updatedTokens = activePlayer.tokens.map {
                if (it.id == tokenId) updatedToken else it
            }

            // Check if captured opponent
            val capturedTokens = LudoRules.findCapturedTokens(
                allPlayers = state.players,
                movingPlayerColor = activePlayer.color,
                targetStep = targetStep
            )

            var bonusTurn = (dice == 6)

            if (capturedTokens.isNotEmpty()) {
                soundManager.playCapture()
                bonusTurn = true
                for (cap in capturedTokens) {
                    addLog("⚔️ ${activePlayer.name} captured ${cap.color.displayName}'s token!", activePlayer.color)
                }
            }

            if (targetStep == 56) {
                soundManager.playHomeReach()
                bonusTurn = true
                addLog("🏁 ${activePlayer.name}'s token reached HOME!", activePlayer.color)
            }

            // Update all players (including captured tokens sent to yard)
            val updatedPlayers = state.players.map { player ->
                if (player.color == activePlayer.color) {
                    val isFinished = updatedTokens.all { it.isHome }
                    player.copy(
                        tokens = updatedTokens,
                        hasWon = isFinished
                    )
                } else {
                    // Reset any captured tokens of this opponent
                    val hasCaptured = capturedTokens.any { it.color == player.color }
                    if (hasCaptured) {
                        val capturedIds = capturedTokens.filter { it.color == player.color }.map { it.id }.toSet()
                        val newOppTokens = player.tokens.map { oppToken ->
                            if (oppToken.id in capturedIds) {
                                oppToken.copy(stepCount = -1)
                            } else {
                                oppToken
                            }
                        }
                        player.copy(tokens = newOppTokens)
                    } else {
                        player
                    }
                }
            }

            // Check if active player just won
            val updatedActivePlayer = updatedPlayers.first { it.color == activePlayer.color }
            var newRankings = state.finishRankings
            var winner = state.winner

            if (updatedActivePlayer.hasWon && !state.finishRankings.any { it.color == activePlayer.color }) {
                val rank = newRankings.size + 1
                val rankedPlayer = updatedActivePlayer.copy(finishRank = rank)
                newRankings = newRankings + rankedPlayer
                if (winner == null) {
                    winner = rankedPlayer
                    soundManager.playVictory()
                    addLog("🏆 WINNER! ${activePlayer.name} has won the game in 1st Place! 🏆", activePlayer.color)
                }
            }

            // Check overall game over: if only 1 active player left or 1st place won in 2-player
            val remainingActive = updatedPlayers.count { !it.hasWon }
            val isGameOver = (updatedPlayers.size <= 2 && winner != null) || remainingActive <= 1

            if (isGameOver) {
                _gameState.update {
                    it.copy(
                        players = updatedPlayers,
                        finishRankings = newRankings,
                        winner = winner,
                        turnPhase = TurnPhase.GAME_OVER,
                        bonusRollAwarded = false
                    )
                }
                saveGame()
                return@launch
            }

            _gameState.update {
                it.copy(
                    players = updatedPlayers,
                    finishRankings = newRankings,
                    winner = winner,
                    bonusRollAwarded = bonusTurn
                )
            }

            saveGame()

            val endDelay = if (state.fastAiSpeed) 200L else 350L
            delay(endDelay)

            advanceToNextTurn(bonus = bonusTurn)
        }
    }

    private fun advanceToNextTurn(bonus: Boolean) {
        val state = _gameState.value
        if (state.turnPhase == TurnPhase.GAME_OVER) return

        var nextIndex = state.activePlayerIndex
        var newConsecutiveSixes = state.consecutiveSixes

        if (!bonus) {
            newConsecutiveSixes = 0
            // Find next non-finished player
            val playerCount = state.players.size
            for (i in 1..playerCount) {
                val candidateIdx = (state.activePlayerIndex + i) % playerCount
                if (!state.players[candidateIdx].hasWon) {
                    nextIndex = candidateIdx
                    break
                }
            }
        } else {
            addLog("⭐ ${state.activePlayer?.name} earned a bonus roll!", state.activePlayer?.color)
        }

        _gameState.update {
            it.copy(
                activePlayerIndex = nextIndex,
                isDiceRolled = false,
                turnPhase = TurnPhase.WAITING_FOR_ROLL,
                eligibleTokenIds = emptySet(),
                consecutiveSixes = newConsecutiveSixes,
                bonusRollAwarded = false
            )
        }

        saveGame()
        checkAiTurn()
    }

    private fun checkAiTurn() {
        val state = _gameState.value
        val activePlayer = state.activePlayer ?: return

        if (state.turnPhase != TurnPhase.WAITING_FOR_ROLL) return
        if (activePlayer.type == PlayerType.COMPUTER && !activePlayer.hasWon) {
            aiTurnJob?.cancel()
            aiTurnJob = viewModelScope.launch {
                val delayTime = if (state.fastAiSpeed) 400L else 750L
                delay(delayTime)
                rollDice()
            }
        }
    }

    fun toggleSound() {
        soundManager.isSoundEnabled = !soundManager.isSoundEnabled
        _gameState.update { it.copy(soundEnabled = soundManager.isSoundEnabled) }
        soundManager.playClick()
        saveGame()
    }

    fun toggleFastAiSpeed() {
        val newSpeed = !_gameState.value.fastAiSpeed
        _gameState.update { it.copy(fastAiSpeed = newSpeed) }
        soundManager.playClick()
        saveGame()
    }

    private fun addLog(text: String, color: PlayerColor?) {
        val entry = GameLogEntry(text = text, playerColor = color)
        _gameState.update {
            val updatedLogs = (listOf(entry) + it.logs).take(25)
            it.copy(logs = updatedLogs)
        }
    }

    private fun saveGame() {
        try {
            val state = _gameState.value
            val editor = prefs.edit()
            editor.putString("game_mode", state.gameMode.name)
            editor.putInt("ai_count", state.computerAiCount)
            editor.putInt("active_index", state.activePlayerIndex)
            editor.putBoolean("sound_enabled", state.soundEnabled)
            editor.putBoolean("fast_ai", state.fastAiSpeed)

            // Save tokens positions: player_idx:token_0,token_1,token_2,token_3
            val playerProgress = state.players.joinToString(";") { player ->
                val tokensStr = player.tokens.joinToString(",") { it.stepCount.toString() }
                "${player.color.name}|$tokensStr|${player.type.name}|${player.hasWon}"
            }
            editor.putString("players_data", playerProgress)
            editor.apply()
        } catch (_: Exception) {
            // Silently handle save exceptions
        }
    }

    private fun restoreSavedGame(): Boolean {
        try {
            val modeName = prefs.getString("game_mode", null) ?: return false
            val mode = GameMode.valueOf(modeName)
            val aiCount = prefs.getInt("ai_count", 3)
            val activeIdx = prefs.getInt("active_index", 0)
            val sound = prefs.getBoolean("sound_enabled", true)
            val fastAi = prefs.getBoolean("fast_ai", false)
            val playersData = prefs.getString("players_data", null) ?: return false

            soundManager.isSoundEnabled = sound

            val playerBlocks = playersData.split(";")
            val players = playerBlocks.mapNotNull { block ->
                val parts = block.split("|")
                if (parts.size >= 4) {
                    val color = PlayerColor.valueOf(parts[0])
                    val tokenSteps = parts[1].split(",").mapNotNull { it.toIntOrNull() }
                    val type = PlayerType.valueOf(parts[2])
                    val hasWon = parts[3].toBoolean()
                    val tokens = List(4) { idx ->
                        Token(id = idx, color = color, stepCount = tokenSteps.getOrElse(idx) { -1 })
                    }
                    val name = when {
                        mode == GameMode.VS_COMPUTER && color == PlayerColor.RED -> "You (Red)"
                        mode == GameMode.VS_COMPUTER -> "Bot ${color.displayName}"
                        else -> "Player ${color.displayName}"
                    }
                    Player(color = color, name = name, type = type, tokens = tokens, hasWon = hasWon)
                } else null
            }

            if (players.isNotEmpty()) {
                _gameState.value = LudoGameState(
                    gameMode = mode,
                    players = players,
                    activePlayerIndex = activeIdx.coerceIn(0, players.size - 1),
                    currentDiceValue = 6,
                    isDiceRolled = false,
                    turnPhase = TurnPhase.WAITING_FOR_ROLL,
                    eligibleTokenIds = emptySet(),
                    soundEnabled = sound,
                    fastAiSpeed = fastAi,
                    computerAiCount = aiCount,
                    logs = listOf(GameLogEntry(text = "Resumed previous Ludo game."))
                )
                checkAiTurn()
                return true
            }
        } catch (_: Exception) {
            // If corrupt, fallback to fresh
        }
        return false
    }
}

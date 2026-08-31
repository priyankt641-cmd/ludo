package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * The 4 classic player colors in Ludo.
 * Standard clockwise order: RED -> GREEN -> YELLOW -> BLUE
 */
enum class PlayerColor(
    val displayName: String,
    val primaryColor: Color,
    val lightColor: Color,
    val darkColor: Color,
    val accentColor: Color,
    val startTrackIndex: Int, // Index in the 52-cell global track
    val homeEntryIndex: Int   // Global track index where player enters their home corridor
) {
    RED(
        displayName = "Red",
        primaryColor = Color(0xFFF43F5E), // Rose-500
        lightColor = Color(0xFFFFE4E6),   // Rose-100
        darkColor = Color(0xFFBE123C),    // Rose-700
        accentColor = Color(0xFFFB7185),  // Rose-400
        startTrackIndex = 0,
        homeEntryIndex = 50
    ),
    GREEN(
        displayName = "Green",
        primaryColor = Color(0xFF10B981), // Emerald-500
        lightColor = Color(0xFFD1FAE5),   // Emerald-100
        darkColor = Color(0xFF047857),    // Emerald-700
        accentColor = Color(0xFF34D399),  // Emerald-400
        startTrackIndex = 13,
        homeEntryIndex = 11
    ),
    YELLOW(
        displayName = "Yellow",
        primaryColor = Color(0xFFFBBF24), // Amber-400
        lightColor = Color(0xFFFEF3C7),   // Amber-100
        darkColor = Color(0xFFD97706),    // Amber-600
        accentColor = Color(0xFFFDE68A),  // Amber-200
        startTrackIndex = 26,
        homeEntryIndex = 24
    ),
    BLUE(
        displayName = "Blue",
        primaryColor = Color(0xFF0EA5E9), // Sky-500
        lightColor = Color(0xFFE0F2FE),   // Sky-100
        darkColor = Color(0xFF0369A1),    // Sky-700
        accentColor = Color(0xFF38BDF8),  // Sky-400
        startTrackIndex = 39,
        homeEntryIndex = 37
    );

    val yardRow: Int
        get() = when (this) {
            RED -> 0
            GREEN -> 0
            YELLOW -> 9
            BLUE -> 9
        }

    val yardCol: Int
        get() = when (this) {
            RED -> 0
            GREEN -> 9
            YELLOW -> 9
            BLUE -> 0
        }
}

enum class PlayerType {
    HUMAN,
    COMPUTER
}

enum class TokenState {
    IN_YARD,
    ON_TRACK,
    FINISHED
}

data class Token(
    val id: Int, // 0..3
    val color: PlayerColor,
    val stepCount: Int = -1 // -1 = IN_YARD, 0..50 = ON_COMMON_TRACK, 51..55 = HOME_CORRIDOR, 56 = GOAL / FINISHED
) {
    val state: TokenState
        get() = when {
            stepCount < 0 -> TokenState.IN_YARD
            stepCount >= 56 -> TokenState.FINISHED
            else -> TokenState.ON_TRACK
        }

    val isHome: Boolean get() = stepCount >= 56
    val isInYard: Boolean get() = stepCount < 0
    val isOnTrack: Boolean get() = stepCount in 0..55
}

data class Player(
    val color: PlayerColor,
    val name: String,
    val type: PlayerType = PlayerType.HUMAN,
    val tokens: List<Token> = List(4) { Token(id = it, color = color) },
    val hasWon: Boolean = false,
    val finishRank: Int = 0
) {
    val tokensHomeCount: Int get() = tokens.count { it.isHome }
    val isComplete: Boolean get() = tokens.all { it.isHome }
}

enum class GameMode(val title: String, val description: String) {
    TWO_PLAYER("2 Players", "Red vs Yellow (Head to Head)"),
    THREE_PLAYER("3 Players", "Red vs Green vs Yellow"),
    FOUR_PLAYER("4 Players", "Red vs Green vs Yellow vs Blue"),
    VS_COMPUTER("vs Computer", "You (Red) vs Smart AI Bots")
}

enum class TurnPhase {
    WAITING_FOR_ROLL,
    ROLLING_DICE,
    SELECTING_TOKEN,
    ANIMATING_MOVE,
    ROUND_TRANSITION,
    GAME_OVER
}

data class BoardCoordinate(
    val row: Int,
    val col: Int
)

data class GameLogEntry(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val playerColor: PlayerColor? = null
)

data class MoveAnimationState(
    val token: Token,
    val path: List<BoardCoordinate>,
    val currentStepIndex: Int = 0
)

data class LudoGameState(
    val gameMode: GameMode = GameMode.FOUR_PLAYER,
    val players: List<Player> = emptyList(),
    val activePlayerIndex: Int = 0,
    val currentDiceValue: Int = 6,
    val isDiceRolled: Boolean = false,
    val turnPhase: TurnPhase = TurnPhase.WAITING_FOR_ROLL,
    val eligibleTokenIds: Set<Int> = emptySet(),
    val consecutiveSixes: Int = 0,
    val winner: Player? = null,
    val finishRankings: List<Player> = emptyList(),
    val logs: List<GameLogEntry> = emptyList(),
    val bonusRollAwarded: Boolean = false,
    val lastCapturedToken: Token? = null,
    val soundEnabled: Boolean = true,
    val fastAiSpeed: Boolean = false,
    val computerAiCount: Int = 3 // For VS_COMPUTER: 1, 2, or 3 AI opponents
) {
    val activePlayer: Player?
        get() = if (players.isNotEmpty() && activePlayerIndex in players.indices) players[activePlayerIndex] else null
}

package com.example.engine

import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.Token

object LudoRules {

    /**
     * Returns a set of token IDs that are legally movable with the given dice value.
     */
    fun getEligibleTokens(player: Player, diceValue: Int): Set<Int> {
        val eligible = mutableSetOf<Int>()
        for (token in player.tokens) {
            if (canMoveToken(token, diceValue)) {
                eligible.add(token.id)
            }
        }
        return eligible
    }

    /**
     * Check if a specific token can legally move with the rolled dice value.
     */
    fun canMoveToken(token: Token, diceValue: Int): Boolean {
        if (token.isHome) return false

        if (token.isInYard) {
            // Can only exit yard on a 6
            return diceValue == 6
        }

        // On track: must not overshoot 56 (exact roll required to finish)
        return (token.stepCount + diceValue) <= 56
    }

    /**
     * Calculate target stepCount for a token after moving by diceValue.
     */
    fun getTargetStepCount(token: Token, diceValue: Int): Int {
        return if (token.isInYard) {
            0
        } else {
            token.stepCount + diceValue
        }
    }

    /**
     * Find if any opponent tokens are captured when a token of [playerColor] lands on [targetStep].
     * Returns the list of captured opponent tokens.
     */
    fun findCapturedTokens(
        allPlayers: List<Player>,
        movingPlayerColor: PlayerColor,
        targetStep: Int
    ): List<Token> {
        // Can only capture on the common track (0..50)
        if (targetStep !in 0..50) return emptyList()

        val landingGlobalIdx = (movingPlayerColor.startTrackIndex + targetStep) % 52

        // Cannot capture on safe star squares
        if (LudoPathManager.safeTrackIndices.contains(landingGlobalIdx)) {
            return emptyList()
        }

        val captured = mutableListOf<Token>()
        for (player in allPlayers) {
            if (player.color == movingPlayerColor) continue

            for (token in player.tokens) {
                val oppGlobalIdx = LudoPathManager.getGlobalTrackIndex(token)
                if (oppGlobalIdx == landingGlobalIdx) {
                    captured.add(token)
                }
            }
        }
        return captured
    }

    /**
     * Check if all tokens of a player have reached home (step 56).
     */
    fun isPlayerFinished(player: Player): Boolean {
        return player.tokens.all { it.isHome }
    }
}

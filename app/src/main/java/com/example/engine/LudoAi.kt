package com.example.engine

import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.Token

/**
 * Intelligent decision-making engine for Computer AI players.
 */
object LudoAi {

    /**
     * Evaluates valid candidate token moves and chooses the highest scoring one.
     */
    fun chooseBestTokenToMove(
        activePlayer: Player,
        allPlayers: List<Player>,
        diceValue: Int,
        eligibleTokenIds: Set<Int>
    ): Int? {
        if (eligibleTokenIds.isEmpty()) return null
        if (eligibleTokenIds.size == 1) return eligibleTokenIds.first()

        val candidates = activePlayer.tokens.filter { eligibleTokenIds.contains(it.id) }
        var bestScore = Int.MIN_VALUE
        var chosenTokenId: Int = candidates.first().id

        for (token in candidates) {
            val score = evaluateMove(token, activePlayer, allPlayers, diceValue)
            if (score > bestScore) {
                bestScore = score
                chosenTokenId = token.id
            }
        }

        return chosenTokenId
    }

    private fun evaluateMove(
        token: Token,
        activePlayer: Player,
        allPlayers: List<Player>,
        diceValue: Int
    ): Int {
        val targetStep = LudoRules.getTargetStepCount(token, diceValue)
        var score = 0

        // 1. Winning move: Token enters finish home (step 56)
        if (targetStep == 56) {
            return 2000
        }

        // 2. Capture opponent token
        val captures = LudoRules.findCapturedTokens(allPlayers, activePlayer.color, targetStep)
        if (captures.isNotEmpty()) {
            // Priority boost for capturing opponent
            score += 1200 + (captures.size * 200)
        }

        // 3. Bringing token out of yard on a 6
        if (token.isInYard && diceValue == 6) {
            // Great priority to bring out pieces to maximize board control
            val piecesOnTrack = activePlayer.tokens.count { it.isOnTrack }
            score += 800 - (piecesOnTrack * 80)
        }

        // 4. Entering the home corridor (steps 51..55) - completely safe from captures
        if (targetStep >= 51 && token.stepCount < 51) {
            score += 650
        }

        // 5. Landing on a Star/Safe square
        if (targetStep in 0..50) {
            val landingGlobalIdx = (activePlayer.color.startTrackIndex + targetStep) % 52
            if (LudoPathManager.safeTrackIndices.contains(landingGlobalIdx)) {
                score += 350
            }
        }

        // 6. Evading imminent threat (if current token position is vulnerable to an enemy token 1..6 squares behind)
        if (token.isOnTrack && token.stepCount <= 50) {
            val currentGlobalIdx = LudoPathManager.getGlobalTrackIndex(token)
            if (currentGlobalIdx != null && !LudoPathManager.safeTrackIndices.contains(currentGlobalIdx)) {
                val isUnderThreat = isTokenUnderThreat(currentGlobalIdx, activePlayer.color, allPlayers)
                if (isUnderThreat) {
                    score += 450 // Incentive to move threatened token away
                }
            }
        }

        // 7. General forward progress towards home
        score += targetStep * 5

        return score
    }

    private fun isTokenUnderThreat(
        targetGlobalIdx: Int,
        myColor: PlayerColor,
        allPlayers: List<Player>
    ): Boolean {
        for (player in allPlayers) {
            if (player.color == myColor) continue
            for (token in player.tokens) {
                val oppGlobal = LudoPathManager.getGlobalTrackIndex(token) ?: continue
                // Distance in clockwise direction
                val dist = (targetGlobalIdx - oppGlobal + 52) % 52
                if (dist in 1..6) {
                    return true
                }
            }
        }
        return false
    }
}

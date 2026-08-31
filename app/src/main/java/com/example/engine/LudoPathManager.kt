package com.example.engine

import com.example.model.BoardCoordinate
import com.example.model.PlayerColor
import com.example.model.Token

/**
 * Manages all coordinates, paths, and positions on the 15x15 Ludo board.
 */
object LudoPathManager {

    /**
     * 52-cell main track in clockwise order, starting from Red's start cell (Row 6, Col 1).
     */
    val commonTrack: List<BoardCoordinate> = listOf(
        // Red arm going right (0..4)
        BoardCoordinate(6, 1),  // 0: Red Start (Safe)
        BoardCoordinate(6, 2),  // 1
        BoardCoordinate(6, 3),  // 2
        BoardCoordinate(6, 4),  // 3
        BoardCoordinate(6, 5),  // 4
        // Green arm going up (5..10)
        BoardCoordinate(5, 6),  // 5
        BoardCoordinate(4, 6),  // 6
        BoardCoordinate(3, 6),  // 7
        BoardCoordinate(2, 6),  // 8: Star Safe
        BoardCoordinate(1, 6),  // 9
        BoardCoordinate(0, 6),  // 10
        // Top turn (11..12)
        BoardCoordinate(0, 7),  // 11
        BoardCoordinate(0, 8),  // 12
        // Green arm going down (13..17)
        BoardCoordinate(1, 8),  // 13: Green Start (Safe)
        BoardCoordinate(2, 8),  // 14
        BoardCoordinate(3, 8),  // 15
        BoardCoordinate(4, 8),  // 16
        BoardCoordinate(5, 8),  // 17
        // Yellow arm going right (18..23)
        BoardCoordinate(6, 9),  // 18
        BoardCoordinate(6, 10), // 19
        BoardCoordinate(6, 11), // 20
        BoardCoordinate(6, 12), // 21: Star Safe
        BoardCoordinate(6, 13), // 22
        BoardCoordinate(6, 14), // 23
        // Right turn (24..25)
        BoardCoordinate(7, 14), // 24
        BoardCoordinate(8, 14), // 25
        // Yellow arm going left (26..30)
        BoardCoordinate(8, 13), // 26: Yellow Start (Safe)
        BoardCoordinate(8, 12), // 27
        BoardCoordinate(8, 11), // 28
        BoardCoordinate(8, 10), // 29
        BoardCoordinate(8, 9),  // 30
        // Blue arm going down (31..36)
        BoardCoordinate(9, 8),  // 31
        BoardCoordinate(10, 8), // 32
        BoardCoordinate(11, 8), // 33
        BoardCoordinate(12, 8), // 34: Star Safe
        BoardCoordinate(13, 8), // 35
        BoardCoordinate(14, 8), // 36
        // Bottom turn (37..38)
        BoardCoordinate(14, 7), // 37
        BoardCoordinate(14, 6), // 38
        // Blue arm going up (39..43)
        BoardCoordinate(13, 6), // 39: Blue Start (Safe)
        BoardCoordinate(12, 6), // 40
        BoardCoordinate(11, 6), // 41
        BoardCoordinate(10, 6), // 42
        BoardCoordinate(9, 6),  // 43
        // Red arm going left (44..49)
        BoardCoordinate(8, 5),  // 44
        BoardCoordinate(8, 4),  // 45
        BoardCoordinate(8, 3),  // 46
        BoardCoordinate(8, 2),  // 47: Star Safe
        BoardCoordinate(8, 1),  // 48
        BoardCoordinate(8, 0),  // 49
        // Left turn (50..51)
        BoardCoordinate(7, 0),  // 50
        BoardCoordinate(6, 0)   // 51
    )

    /**
     * Safe cells on the common track (indices in commonTrack list).
     */
    val safeTrackIndices: Set<Int> = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    /**
     * Safe coordinates corresponding to safe indices
     */
    val safeCoordinates: Set<BoardCoordinate> = safeTrackIndices.map { commonTrack[it] }.toSet()

    /**
     * Home corridors (steps 51..55) for each player
     */
    private val redHomeCorridor = listOf(
        BoardCoordinate(7, 1),
        BoardCoordinate(7, 2),
        BoardCoordinate(7, 3),
        BoardCoordinate(7, 4),
        BoardCoordinate(7, 5)
    )

    private val greenHomeCorridor = listOf(
        BoardCoordinate(1, 7),
        BoardCoordinate(2, 7),
        BoardCoordinate(3, 7),
        BoardCoordinate(4, 7),
        BoardCoordinate(5, 7)
    )

    private val yellowHomeCorridor = listOf(
        BoardCoordinate(7, 13),
        BoardCoordinate(7, 12),
        BoardCoordinate(7, 11),
        BoardCoordinate(7, 10),
        BoardCoordinate(7, 9)
    )

    private val blueHomeCorridor = listOf(
        BoardCoordinate(13, 7),
        BoardCoordinate(12, 7),
        BoardCoordinate(11, 7),
        BoardCoordinate(10, 7),
        BoardCoordinate(9, 7)
    )

    /**
     * Center finish coordinate for each player
     */
    val finishCoordinate: Map<PlayerColor, BoardCoordinate> = mapOf(
        PlayerColor.RED to BoardCoordinate(7, 6),
        PlayerColor.GREEN to BoardCoordinate(6, 7),
        PlayerColor.YELLOW to BoardCoordinate(7, 8),
        PlayerColor.BLUE to BoardCoordinate(8, 7)
    )

    /**
     * Yard token positions for each player (4 tokens per yard)
     */
    val yardSlots: Map<PlayerColor, List<BoardCoordinate>> = mapOf(
        PlayerColor.RED to listOf(
            BoardCoordinate(1, 1),
            BoardCoordinate(1, 4),
            BoardCoordinate(4, 1),
            BoardCoordinate(4, 4)
        ),
        PlayerColor.GREEN to listOf(
            BoardCoordinate(1, 10),
            BoardCoordinate(1, 13),
            BoardCoordinate(4, 10),
            BoardCoordinate(4, 13)
        ),
        PlayerColor.YELLOW to listOf(
            BoardCoordinate(10, 10),
            BoardCoordinate(10, 13),
            BoardCoordinate(13, 10),
            BoardCoordinate(13, 13)
        ),
        PlayerColor.BLUE to listOf(
            BoardCoordinate(10, 1),
            BoardCoordinate(10, 4),
            BoardCoordinate(13, 1),
            BoardCoordinate(13, 4)
        )
    )

    /**
     * Get the board coordinate for a token at its current stepCount
     */
    fun getCoordinate(token: Token): BoardCoordinate {
        if (token.isInYard) {
            val slots = yardSlots[token.color] ?: error("Invalid player color")
            return slots[token.id.coerceIn(0, 3)]
        }

        if (token.stepCount == 56) {
            return finishCoordinate[token.color] ?: BoardCoordinate(7, 7)
        }

        if (token.stepCount in 0..50) {
            val globalIdx = (token.color.startTrackIndex + token.stepCount) % 52
            return commonTrack[globalIdx]
        }

        // Home corridor (51..55)
        val corridorIdx = token.stepCount - 51
        val corridor = when (token.color) {
            PlayerColor.RED -> redHomeCorridor
            PlayerColor.GREEN -> greenHomeCorridor
            PlayerColor.YELLOW -> yellowHomeCorridor
            PlayerColor.BLUE -> blueHomeCorridor
        }
        return corridor[corridorIdx.coerceIn(0, corridor.size - 1)]
    }

    /**
     * Get the global track index (0..51) for a token if it's on the common track (0..50).
     * Returns null if token is in yard, in home corridor, or finished.
     */
    fun getGlobalTrackIndex(token: Token): Int? {
        if (token.stepCount in 0..50) {
            return (token.color.startTrackIndex + token.stepCount) % 52
        }
        return null
    }

    /**
     * Generates a step-by-step list of coordinates from fromStep to toStep for animation.
     */
    fun getPathCoordinates(color: PlayerColor, fromStep: Int, toStep: Int): List<BoardCoordinate> {
        val result = mutableListOf<BoardCoordinate>()
        if (fromStep == -1 && toStep == 0) {
            // Exiting yard to start square
            result.add(commonTrack[color.startTrackIndex])
            return result
        }

        val start = fromStep.coerceAtLeast(0)
        for (step in start..toStep) {
            val dummyToken = Token(id = 0, color = color, stepCount = step)
            result.add(getCoordinate(dummyToken))
        }
        return result
    }

    /**
     * Check if a specific board coordinate is a Star/Safe cell.
     */
    fun isSafeCoordinate(coord: BoardCoordinate): Boolean {
        return safeCoordinates.contains(coord)
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.LudoPathManager
import com.example.engine.LudoRules
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.Token
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Classic Ludo", appName)
    }

    @Test
    fun `token in yard requires 6 to exit`() {
        val token = Token(id = 0, color = PlayerColor.RED, stepCount = -1)
        assertFalse(LudoRules.canMoveToken(token, 5))
        assertFalse(LudoRules.canMoveToken(token, 1))
        assertTrue(LudoRules.canMoveToken(token, 6))
        assertEquals(0, LudoRules.getTargetStepCount(token, 6))
    }

    @Test
    fun `token on track moves by dice roll`() {
        val token = Token(id = 0, color = PlayerColor.RED, stepCount = 10)
        assertTrue(LudoRules.canMoveToken(token, 4))
        assertEquals(14, LudoRules.getTargetStepCount(token, 4))
    }

    @Test
    fun `token requires exact roll to reach finish`() {
        val token = Token(id = 0, color = PlayerColor.RED, stepCount = 54)
        assertTrue(LudoRules.canMoveToken(token, 2))
        assertFalse(LudoRules.canMoveToken(token, 3)) // Overshoots 56
    }

    @Test
    fun `capture opponent token on common track non-safe cell`() {
        val redPlayer = Player(
            color = PlayerColor.RED,
            name = "Red",
            tokens = listOf(Token(id = 0, color = PlayerColor.RED, stepCount = 10)) // target step will be 13
        )
        // Green token on its start square (stepCount = 0). Green start index = 13.
        // Red start index = 0. Red at step 13 -> global index = 13.
        // Green at step 0 -> global index = 13. But global 13 is Green Start (SAFE)!
        // Safe cell should NOT be captured:
        val greenPlayer = Player(
            color = PlayerColor.GREEN,
            name = "Green",
            tokens = listOf(Token(id = 0, color = PlayerColor.GREEN, stepCount = 0))
        )
        val capturesSafe = LudoRules.findCapturedTokens(
            allPlayers = listOf(redPlayer, greenPlayer),
            movingPlayerColor = PlayerColor.RED,
            targetStep = 13
        )
        assertTrue(capturesSafe.isEmpty()) // Safe cell protects token

        // Opponent on a non-safe cell: e.g. Red targetStep = 14 (global 14).
        // Green at stepCount = 1 (global 13 + 1 = 14). Non-safe!
        val greenPlayer2 = Player(
            color = PlayerColor.GREEN,
            name = "Green",
            tokens = listOf(Token(id = 0, color = PlayerColor.GREEN, stepCount = 1))
        )
        val capturesNonSafe = LudoRules.findCapturedTokens(
            allPlayers = listOf(redPlayer, greenPlayer2),
            movingPlayerColor = PlayerColor.RED,
            targetStep = 14
        )
        assertEquals(1, capturesNonSafe.size)
        assertEquals(PlayerColor.GREEN, capturesNonSafe[0].color)
    }
}


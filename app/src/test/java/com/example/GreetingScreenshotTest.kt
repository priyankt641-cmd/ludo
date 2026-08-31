package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.model.GameMode
import com.example.model.LudoGameState
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.PlayerType
import com.example.ui.LudoBoardView
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ludo_board_screenshot() {
        val players = listOf(
            Player(color = PlayerColor.RED, name = "Red", type = PlayerType.HUMAN),
            Player(color = PlayerColor.GREEN, name = "Green", type = PlayerType.HUMAN),
            Player(color = PlayerColor.YELLOW, name = "Yellow", type = PlayerType.HUMAN),
            Player(color = PlayerColor.BLUE, name = "Blue", type = PlayerType.HUMAN)
        )
        val state = LudoGameState(
            gameMode = GameMode.FOUR_PLAYER,
            players = players,
            activePlayerIndex = 0
        )

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                Box(modifier = Modifier.size(360.dp)) {
                    LudoBoardView(
                        gameState = state,
                        animatingToken = null,
                        animatingStep = -1,
                        onTokenClick = {}
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}


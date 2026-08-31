package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.PlayerType
import com.example.model.TurnPhase
import com.example.ui.theme.BrandOnPrimary
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryContainer
import com.example.ui.theme.BrandOnPrimaryContainer
import com.example.viewmodel.LudoViewModel

@Composable
fun LudoGameScreen(
    viewModel: LudoViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val animatingToken by viewModel.animatingToken.collectAsState()
    val animatingStep by viewModel.animatingStep.collectAsState()

    var showModeDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }

    val activePlayer = gameState.activePlayer
    val isHumanTurn = activePlayer?.type == PlayerType.HUMAN
    val canRoll = isHumanTurn && (gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL)

    Scaffold(
        containerColor = Color(0xFFF3F4F9),
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("ludo_game_screen")
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isLandscape = maxWidth > maxHeight && maxWidth > 650.dp

            if (isLandscape) {
                // Landscape / Tablet layout: Side-by-side
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Panel: Board
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1.15f)
                            .fillMaxHeight()
                    ) {
                        LudoBoardView(
                            gameState = gameState,
                            animatingToken = animatingToken,
                            animatingStep = animatingStep,
                            onTokenClick = { tokenId -> viewModel.selectToken(tokenId) },
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = 540.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right Panel: Controls & Info
                    Column(
                        modifier = Modifier
                            .weight(0.85f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        HeaderBar(
                            gameMode = gameState.gameMode,
                            soundEnabled = gameState.soundEnabled,
                            fastAi = gameState.fastAiSpeed,
                            onOpenModes = { showModeDialog = true },
                            onOpenRules = { showRulesDialog = true },
                            onToggleSound = { viewModel.toggleSound() },
                            onToggleSpeed = { viewModel.toggleFastAiSpeed() },
                            onRestart = { showRestartDialog = true }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Current Turn Card
                        CurrentTurnCard(gameState = gameState)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Players List
                        PlayersRow(
                            players = gameState.players,
                            activePlayerIndex = gameState.activePlayerIndex
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Turn & Dice Controls
                        TurnControlDeck(
                            gameState = gameState,
                            canRoll = canRoll,
                            onRollDice = { viewModel.rollDice() }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Game Activity Log
                        GameLogTicker(logs = gameState.logs)
                    }
                }
            } else {
                // Portrait Mobile Layout: Vertical stack
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Top Header Bar
                    HeaderBar(
                        gameMode = gameState.gameMode,
                        soundEnabled = gameState.soundEnabled,
                        fastAi = gameState.fastAiSpeed,
                        onOpenModes = { showModeDialog = true },
                        onOpenRules = { showRulesDialog = true },
                        onToggleSound = { viewModel.toggleSound() },
                        onToggleSpeed = { viewModel.toggleFastAiSpeed() },
                        onRestart = { showRestartDialog = true }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Current Active Turn Banner Card
                    CurrentTurnCard(
                        gameState = gameState,
                        modifier = Modifier.widthIn(max = 440.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Players Row
                    PlayersRow(
                        players = gameState.players,
                        activePlayerIndex = gameState.activePlayerIndex
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Center Responsive Ludo Board
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .widthIn(max = 440.dp)
                    ) {
                        LudoBoardView(
                            gameState = gameState,
                            animatingToken = animatingToken,
                            animatingStep = animatingStep,
                            onTokenClick = { tokenId -> viewModel.selectToken(tokenId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Turn & Dice Control Deck
                    TurnControlDeck(
                        gameState = gameState,
                        canRoll = canRoll,
                        onRollDice = { viewModel.rollDice() },
                        modifier = Modifier.widthIn(max = 440.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Game Activity ticker
                    GameLogTicker(
                        logs = gameState.logs,
                        modifier = Modifier.widthIn(max = 440.dp)
                    )
                }
            }
        }
    }

    // Dialogs
    if (showModeDialog) {
        ModeSelectDialog(
            currentMode = gameState.gameMode,
            currentAiCount = gameState.computerAiCount,
            onDismiss = { showModeDialog = false },
            onSelectMode = { mode, aiCount ->
                showModeDialog = false
                viewModel.initGame(mode, aiCount)
            }
        )
    }

    if (showRulesDialog) {
        RulesDialog(onDismiss = { showRulesDialog = false })
    }

    if (showRestartDialog) {
        RestartConfirmDialog(
            onConfirm = {
                showRestartDialog = false
                viewModel.initGame(gameState.gameMode, gameState.computerAiCount)
            },
            onDismiss = { showRestartDialog = false }
        )
    }

    if (gameState.turnPhase == TurnPhase.GAME_OVER && gameState.winner != null) {
        VictoryDialog(
            winner = gameState.winner,
            rankings = gameState.finishRankings,
            allPlayers = gameState.players,
            onPlayAgain = { viewModel.initGame(gameState.gameMode, gameState.computerAiCount) },
            onChangeMode = { showModeDialog = true }
        )
    }
}

@Composable
private fun HeaderBar(
    gameMode: GameMode,
    soundEnabled: Boolean,
    fastAi: Boolean,
    onOpenModes: () -> Unit,
    onOpenRules: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleSpeed: () -> Unit,
    onRestart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Brand Logo & Mode Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenModes)
                    .padding(4.dp)
                    .testTag("mode_chip_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Ludo Royale",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = (-0.2).sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = gameMode.title.uppercase(),
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Action icons (Speed, Sound, Rules, Restart)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Rules Button
                IconButton(
                    onClick = onOpenRules,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .testTag("rules_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Game Rules",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // AI Speed
                IconButton(
                    onClick = onToggleSpeed,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (fastAi) Color(0xFFD1FAE5) else Color(0xFFF1F5F9))
                        .testTag("speed_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "AI Speed",
                        tint = if (fastAi) Color(0xFF047857) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Sound Toggle
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (soundEnabled) BrandPrimaryContainer else Color(0xFFF1F5F9))
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Toggle Sound",
                        tint = if (soundEnabled) BrandOnPrimaryContainer else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Restart Button
                IconButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE4E6))
                        .testTag("restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Game",
                        tint = Color(0xFFBE123C),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentTurnCard(
    gameState: com.example.model.LudoGameState,
    modifier: Modifier = Modifier
) {
    val activePlayer = gameState.activePlayer ?: return
    val isRolling = gameState.turnPhase == TurnPhase.ROLLING_DICE
    val isSelecting = gameState.turnPhase == TurnPhase.SELECTING_TOKEN
    val isAnimating = gameState.turnPhase == TurnPhase.ANIMATING_MOVE

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .testTag("current_turn_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Left: Player Avatar & Label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(activePlayer.color.accentColor, activePlayer.color.primaryColor)
                            )
                        )
                        .border(2.dp, Color.White, CircleShape)
                        .shadow(4.dp, CircleShape, spotColor = activePlayer.color.primaryColor)
                ) {
                    Text(
                        text = activePlayer.name.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "CURRENT TURN",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = activePlayer.name,
                        color = Color(0xFF0F172A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right: Status indicator badge
            val statusText = when {
                isRolling -> "ROLLING..."
                isAnimating -> "MOVING..."
                isSelecting && activePlayer.type == PlayerType.HUMAN -> "SELECT TOKEN"
                isSelecting -> "BOT MOVING"
                activePlayer.type == PlayerType.COMPUTER -> "BOT TURN"
                gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL -> "ROLL DICE"
                else -> "WAITING"
            }

            val badgeBgColor = when {
                isSelecting -> Color(0xFFFEF3C7) // Amber-100
                gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL -> activePlayer.color.lightColor
                else -> Color(0xFFF1F5F9)
            }

            val badgeTextColor = when {
                isSelecting -> Color(0xFFB45309) // Amber-700
                gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL -> activePlayer.color.darkColor
                else -> Color(0xFF475569)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(badgeBgColor)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = statusText,
                    color = badgeTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun PlayersRow(
    players: List<com.example.model.Player>,
    activePlayerIndex: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        players.forEachIndexed { index, player ->
            PlayerChip(
                player = player,
                isActive = (index == activePlayerIndex),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TurnControlDeck(
    gameState: com.example.model.LudoGameState,
    canRoll: Boolean,
    onRollDice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePlayer = gameState.activePlayer ?: return
    val isRolling = gameState.turnPhase == TurnPhase.ROLLING_DICE
    val isSelecting = gameState.turnPhase == TurnPhase.SELECTING_TOKEN

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1A000000))
            .testTag("turn_control_deck")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Left: Dice and Roll Value status
            Row(verticalAlignment = Alignment.CenterVertically) {
                DiceView(
                    diceValue = gameState.currentDiceValue,
                    isRolling = isRolling,
                    enabled = canRoll,
                    primaryColor = activePlayer.color.primaryColor,
                    size = 58.dp,
                    onClick = onRollDice
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (gameState.currentDiceValue > 0) "ROLLED ${gameState.currentDiceValue}" else "READY",
                        color = if (gameState.currentDiceValue == 6) activePlayer.color.primaryColor else Color(0xFF0F172A),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )

                    val hint = when {
                        isRolling -> "Rolling..."
                        isSelecting -> "Tap your token"
                        canRoll -> "Tap to roll"
                        else -> "Please wait"
                    }

                    Text(
                        text = hint,
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right: Royale Purple Roll Dice Pill Button
            Button(
                onClick = onRollDice,
                enabled = canRoll,
                shape = RoundedCornerShape(50), // Full rounded pill
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE2E8F0),
                    disabledContentColor = Color(0xFF94A3B8)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp
                ),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("roll_dice_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRolling) "Rolling..." else "Roll Dice",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun GameLogTicker(
    logs: List<com.example.model.GameLogEntry>,
    modifier: Modifier = Modifier
) {
    val latest = logs.firstOrNull() ?: return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("game_log_ticker")
    ) {
        if (latest.playerColor != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(latest.playerColor.primaryColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = latest.text,
            color = Color(0xFF475569),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


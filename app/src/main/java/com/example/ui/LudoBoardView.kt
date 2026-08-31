package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.LudoPathManager
import com.example.model.BoardCoordinate
import com.example.model.LudoGameState
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.Token
import com.example.model.TurnPhase
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LudoBoardView(
    gameState: LudoGameState,
    animatingToken: Token?,
    animatingStep: Int,
    onTokenClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = Color(0x331E293B))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B)) // Slate-800 sleek modern frame
            .border(5.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .testTag("ludo_board")
    ) {
        val boardSize = maxWidth
        val cellSize = boardSize / 15f

        // 1. Draw static board graphics on canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLudoBoard(size = size)
        }

        // 2. Render interactive Tokens layer
        TokensLayer(
            gameState = gameState,
            animatingToken = animatingToken,
            animatingStep = animatingStep,
            cellSize = cellSize,
            onTokenClick = onTokenClick
        )
    }
}

/**
 * Draws the entire Ludo board (yards, tracks, safe stars, arrows, finish triangles)
 */
private fun DrawScope.drawLudoBoard(size: Size) {
    val w = size.width
    val cellSize = w / 15f
    val gridBorderColor = Color(0xFFE2E8F0) // Slate-200 clean grid
    val cellBgColor = Color(0xFFF8FAFC)     // Slate-50 crisp cell background

    // Base background
    drawRect(color = Color(0xFFFFFFFF), size = size)

    // Helper to get rect for grid (row, col, rowSpan, colSpan)
    fun gridRect(row: Int, col: Int, rowSpan: Int = 1, colSpan: Int = 1): Rect {
        return Rect(
            left = col * cellSize,
            top = row * cellSize,
            right = (col + colSpan) * cellSize,
            bottom = (row + rowSpan) * cellSize
        )
    }

    // 1. Draw 4 Corner Yards (6x6 cells each)
    val redYard = gridRect(0, 0, 6, 6)
    val greenYard = gridRect(0, 9, 6, 6)
    val yellowYard = gridRect(9, 9, 6, 6)
    val blueYard = gridRect(9, 0, 6, 6)

    drawRect(color = PlayerColor.RED.primaryColor, topLeft = redYard.topLeft, size = redYard.size)
    drawRect(color = PlayerColor.GREEN.primaryColor, topLeft = greenYard.topLeft, size = greenYard.size)
    drawRect(color = PlayerColor.YELLOW.primaryColor, topLeft = yellowYard.topLeft, size = yellowYard.size)
    drawRect(color = PlayerColor.BLUE.primaryColor, topLeft = blueYard.topLeft, size = blueYard.size)

    // Draw inner white bases for yards (4x4 cells)
    fun drawYardInnerBase(row: Int, col: Int, color: PlayerColor) {
        val rect = gridRect(row + 1, col + 1, 4, 4)
        // White rounded card inside yard with soft shadow effect
        drawRoundRect(
            color = Color.White,
            topLeft = rect.topLeft,
            size = rect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cellSize * 0.35f, cellSize * 0.35f)
        )
        drawRoundRect(
            color = Color(0x1A000000),
            topLeft = rect.topLeft,
            size = rect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cellSize * 0.35f, cellSize * 0.35f),
            style = Stroke(width = 1.5f)
        )

        // 4 Token resting slots
        val slots = listOf(
            Offset((col + 1.85f) * cellSize, (row + 1.85f) * cellSize),
            Offset((col + 4.15f) * cellSize, (row + 1.85f) * cellSize),
            Offset((col + 1.85f) * cellSize, (row + 4.15f) * cellSize),
            Offset((col + 4.15f) * cellSize, (row + 4.15f) * cellSize)
        )
        for (slot in slots) {
            drawCircle(
                color = color.lightColor,
                radius = cellSize * 0.68f,
                center = slot
            )
            drawCircle(
                color = color.primaryColor,
                radius = cellSize * 0.54f,
                center = slot
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = cellSize * 0.28f,
                center = slot
            )
        }
    }

    drawYardInnerBase(0, 0, PlayerColor.RED)
    drawYardInnerBase(0, 9, PlayerColor.GREEN)
    drawYardInnerBase(9, 9, PlayerColor.YELLOW)
    drawYardInnerBase(9, 0, PlayerColor.BLUE)

    // 2. Draw 52 track grid cells
    for (r in 0 until 15) {
        for (c in 0 until 15) {
            val isYard = (r < 6 && c < 6) || (r < 6 && c >= 9) || (r >= 9 && c >= 9) || (r >= 9 && c < 6)
            val isCenter = (r in 6..8 && c in 6..8)

            if (!isYard && !isCenter) {
                val cellRect = gridRect(r, c)
                var fillColor = cellBgColor

                // Red Start & Corridor
                if (r == 6 && c == 1) fillColor = PlayerColor.RED.primaryColor
                if (r == 7 && c in 1..5) fillColor = PlayerColor.RED.primaryColor

                // Green Start & Corridor
                if (r == 1 && c == 8) fillColor = PlayerColor.GREEN.primaryColor
                if (c == 7 && r in 1..5) fillColor = PlayerColor.GREEN.primaryColor

                // Yellow Start & Corridor
                if (r == 8 && c == 13) fillColor = PlayerColor.YELLOW.primaryColor
                if (r == 7 && c in 9..13) fillColor = PlayerColor.YELLOW.primaryColor

                // Blue Start & Corridor
                if (r == 13 && c == 6) fillColor = PlayerColor.BLUE.primaryColor
                if (c == 7 && r in 9..13) fillColor = PlayerColor.BLUE.primaryColor

                // Draw Cell Fill
                drawRect(color = fillColor, topLeft = cellRect.topLeft, size = cellRect.size)

                // Cell Grid Border
                drawRect(
                    color = gridBorderColor,
                    topLeft = cellRect.topLeft,
                    size = cellRect.size,
                    style = Stroke(width = 1.0f)
                )

                // Draw Start Arrows
                if (r == 6 && c == 1) drawArrow(this, cellRect.center, 0f, Color.White, cellSize * 0.42f)
                if (r == 1 && c == 8) drawArrow(this, cellRect.center, 90f, Color.White, cellSize * 0.42f)
                if (r == 8 && c == 13) drawArrow(this, cellRect.center, 180f, Color(0xFF78350F), cellSize * 0.42f)
                if (r == 13 && c == 6) drawArrow(this, cellRect.center, 270f, Color.White, cellSize * 0.42f)
            }
        }
    }

    // 3. Draw Safe Stars on the 8 safe checkpoints
    val starCoords = listOf(
        BoardCoordinate(6, 1),  // Red Start
        BoardCoordinate(2, 6),  // Safe Star 1
        BoardCoordinate(1, 8),  // Green Start
        BoardCoordinate(6, 12), // Safe Star 2
        BoardCoordinate(8, 13), // Yellow Start
        BoardCoordinate(12, 8), // Safe Star 3
        BoardCoordinate(13, 6), // Blue Start
        BoardCoordinate(8, 2)   // Safe Star 4
    )

    for (coord in starCoords) {
        val center = Offset((coord.col + 0.5f) * cellSize, (coord.row + 0.5f) * cellSize)
        val isColoredCell = (coord.row == 6 && coord.col == 1) ||
                (coord.row == 1 && coord.col == 8) ||
                (coord.row == 8 && coord.col == 13) ||
                (coord.row == 13 && coord.col == 6)

        val starColor = if (isColoredCell) Color.White.copy(alpha = 0.95f) else Color(0xFFF59E0B)
        drawStar(
            drawScope = this,
            center = center,
            radius = cellSize * 0.35f,
            color = starColor,
            points = 5
        )
    }

    // 4. Center Home 3x3 Finish Area (4 colored triangles meeting at center)
    val centerRect = gridRect(6, 6, 3, 3)
    val mid = centerRect.center

    // Center background slate
    drawRect(color = Color(0xFF1E293B), topLeft = centerRect.topLeft, size = centerRect.size)

    // Red Triangle (Left)
    val redPath = Path().apply {
        moveTo(centerRect.left, centerRect.top)
        lineTo(mid.x, mid.y)
        lineTo(centerRect.left, centerRect.bottom)
        close()
    }
    drawPath(redPath, color = PlayerColor.RED.primaryColor)

    // Green Triangle (Top)
    val greenPath = Path().apply {
        moveTo(centerRect.left, centerRect.top)
        lineTo(mid.x, mid.y)
        lineTo(centerRect.right, centerRect.top)
        close()
    }
    drawPath(greenPath, color = PlayerColor.GREEN.primaryColor)

    // Yellow Triangle (Right)
    val yellowPath = Path().apply {
        moveTo(centerRect.right, centerRect.top)
        lineTo(mid.x, mid.y)
        lineTo(centerRect.right, centerRect.bottom)
        close()
    }
    drawPath(yellowPath, color = PlayerColor.YELLOW.primaryColor)

    // Blue Triangle (Bottom)
    val bluePath = Path().apply {
        moveTo(centerRect.left, centerRect.bottom)
        lineTo(mid.x, mid.y)
        lineTo(centerRect.right, centerRect.bottom)
        close()
    }
    drawPath(bluePath, color = PlayerColor.BLUE.primaryColor)

    // Center Gold Core circle
    drawCircle(
        color = Color(0xFFFFFFFF),
        radius = cellSize * 0.44f,
        center = mid
    )
    drawCircle(
        color = Color(0xFF1E293B),
        radius = cellSize * 0.44f,
        center = mid,
        style = Stroke(width = 2.5f)
    )
    drawStar(this, mid, cellSize * 0.28f, Color(0xFFF59E0B), 5)

    // Center Outer Outline
    drawRect(
        color = Color(0xFF1E293B),
        topLeft = centerRect.topLeft,
        size = centerRect.size,
        style = Stroke(width = 2.0f)
    )

    // Outer Yard Borders
    drawRect(color = Color(0x33000000), topLeft = redYard.topLeft, size = redYard.size, style = Stroke(1.5f))
    drawRect(color = Color(0x33000000), topLeft = greenYard.topLeft, size = greenYard.size, style = Stroke(1.5f))
    drawRect(color = Color(0x33000000), topLeft = yellowYard.topLeft, size = yellowYard.size, style = Stroke(1.5f))
    drawRect(color = Color(0x33000000), topLeft = blueYard.topLeft, size = blueYard.size, style = Stroke(1.5f))
}

private fun drawStar(
    drawScope: DrawScope,
    center: Offset,
    radius: Float,
    color: Color,
    points: Int = 5
) {
    val innerRadius = radius * 0.45f
    val path = Path()
    val angleStep = PI / points

    for (i in 0 until (points * 2)) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = i * angleStep - PI / 2.0
        val x = (center.x + r * cos(angle)).toFloat()
        val y = (center.y + r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawScope.drawPath(path, color = color, style = Fill)
    drawScope.drawPath(path, color = Color(0x33000000), style = Stroke(width = 1.5f))
}

private fun drawArrow(
    drawScope: DrawScope,
    center: Offset,
    rotationDeg: Float,
    color: Color,
    size: Float
) {
    val path = Path()
    val rad = Math.toRadians(rotationDeg.toDouble())
    fun rot(x: Float, y: Float): Offset {
        val rx = x * cos(rad) - y * sin(rad)
        val ry = x * sin(rad) + y * cos(rad)
        return Offset((center.x + rx).toFloat(), (center.y + ry).toFloat())
    }

    val p1 = rot(size * 0.6f, 0f)
    val p2 = rot(-size * 0.5f, -size * 0.45f)
    val p3 = rot(-size * 0.2f, 0f)
    val p4 = rot(-size * 0.5f, size * 0.45f)

    path.moveTo(p1.x, p1.y)
    path.lineTo(p2.x, p2.y)
    path.lineTo(p3.x, p3.y)
    path.lineTo(p4.x, p4.y)
    path.close()

    drawScope.drawPath(path, color = color)
}

/**
 * Tokens Layer: renders all tokens with smooth animations, selection pulses, and stacking offsets.
 */
@Composable
private fun TokensLayer(
    gameState: LudoGameState,
    animatingToken: Token?,
    animatingStep: Int,
    cellSize: Dp,
    onTokenClick: (Int) -> Unit
) {
    val activePlayer = gameState.activePlayer
    val isSelecting = (gameState.turnPhase == TurnPhase.SELECTING_TOKEN)

    // Group non-animating tokens by coordinate to apply cluster offsets when stacked
    val allTokens = gameState.players.flatMap { it.tokens }

    // Map each token to its current board coordinate
    val tokenLocations = allTokens.associateWith { token ->
        if (animatingToken != null && token.color == animatingToken.color && token.id == animatingToken.id) {
            val step = if (animatingStep >= 0) animatingStep else token.stepCount
            val dummy = token.copy(stepCount = step)
            LudoPathManager.getCoordinate(dummy)
        } else {
            LudoPathManager.getCoordinate(token)
        }
    }

    // Group by coordinate to compute multi-token offset
    val groupedByCoord = tokenLocations.entries.groupBy({ it.value }, { it.key })

    for (token in allTokens) {
        val coord = tokenLocations[token] ?: continue
        val isAnimatingThis = (animatingToken != null && token.color == animatingToken.color && token.id == animatingToken.id)
        val isEligible = isSelecting && (activePlayer?.color == token.color) && (token.id in gameState.eligibleTokenIds)

        // Cluster offset if multiple tokens share the exact cell
        val tokensOnSameCell = groupedByCoord[coord] ?: emptyList()
        val tokenIndexInCell = tokensOnSameCell.indexOf(token)
        val totalOnCell = tokensOnSameCell.size

        val (offsetXFraction, offsetYFraction) = calculateClusterOffset(tokenIndexInCell, totalOnCell)

        // Animated token position
        val targetX = (coord.col.toFloat() + 0.5f + offsetXFraction) * cellSize.value
        val targetY = (coord.row.toFloat() + 0.5f + offsetYFraction) * cellSize.value

        val animX by animateFloatAsState(
            targetValue = targetX,
            animationSpec = if (isAnimatingThis) tween(100, easing = FastOutSlowInEasing) else spring(),
            label = "token_x_${token.color}_${token.id}"
        )
        val animY by animateFloatAsState(
            targetValue = targetY,
            animationSpec = if (isAnimatingThis) tween(100, easing = FastOutSlowInEasing) else spring(),
            label = "token_y_${token.color}_${token.id}"
        )

        // Token Size (scales down slightly when clustered)
        val tokenSizeRatio = if (totalOnCell > 1 && !token.isInYard) 0.62f else 0.76f
        val tokenDiameter = cellSize * tokenSizeRatio

        TokenPawn(
            token = token,
            isEligible = isEligible,
            diameter = tokenDiameter,
            xPx = animX.dp,
            yPx = animY.dp,
            onClick = {
                if (isEligible) {
                    onTokenClick(token.id)
                }
            }
        )
    }
}

private fun calculateClusterOffset(index: Int, count: Int): Pair<Float, Float> {
    if (count <= 1 || index < 0) return Pair(0f, 0f)
    return when (count) {
        2 -> if (index == 0) Pair(-0.16f, -0.16f) else Pair(0.16f, 0.16f)
        3 -> when (index) {
            0 -> Pair(0f, -0.2f)
            1 -> Pair(-0.18f, 0.16f)
            else -> Pair(0.18f, 0.16f)
        }
        else -> when (index % 4) {
            0 -> Pair(-0.18f, -0.18f)
            1 -> Pair(0.18f, -0.18f)
            2 -> Pair(-0.18f, 0.18f)
            else -> Pair(0.18f, 0.18f)
        }
    }
}

/**
 * Visual rendering for a 3D Ludo Token Pawn with glossy finish and selection bounce.
 */
@Composable
private fun TokenPawn(
    token: Token,
    isEligible: Boolean,
    diameter: Dp,
    xPx: Dp,
    yPx: Dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_${token.id}")
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_pulse"
    )

    val scale = if (isEligible) bounceScale else 1.0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset(x = xPx - (diameter / 2), y = yPx - (diameter / 2))
            .size(diameter)
            .scale(scale)
            .testTag("token_${token.color.name}_${token.id}")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Selection Glowing Ring
        if (isEligible) {
            Box(
                modifier = Modifier
                    .size(diameter * 1.55f)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFFFFD700).copy(alpha = ringAlpha), CircleShape)
            )
        }

        // 3D Token Pawn
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.width / 2f
            val center = Offset(r, r)

            // Drop shadow
            drawCircle(
                color = Color(0x55000000),
                radius = r * 0.96f,
                center = Offset(center.x + 1.5f, center.y + 2.5f)
            )

            // Outer Base Ring (Gold/Silver accent border)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF9C4),
                        Color(0xFFD4AF37),
                        Color(0xFF8D6E63)
                    ),
                    center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                    radius = r
                ),
                radius = r * 0.95f,
                center = center
            )

            // Inner Colored Gem / Body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        token.color.accentColor,
                        token.color.primaryColor,
                        token.color.darkColor
                    ),
                    center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                    radius = r * 0.85f
                ),
                radius = r * 0.76f,
                center = center
            )

            // Specular Highlight Reflection (Glossy shine)
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                radius = r * 0.26f,
                center = Offset(center.x - r * 0.28f, center.y - r * 0.28f)
            )

            // Center Ring
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = r * 0.12f,
                center = center
            )
        }
    }
}

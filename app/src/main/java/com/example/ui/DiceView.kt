package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DiceView(
    diceValue: Int,
    isRolling: Boolean,
    enabled: Boolean,
    primaryColor: Color,
    size: Dp = 64.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dice_rotation")
    val rollingRotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dice_wobble"
    )
    val rollingScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dice_scale"
    )

    val currentRotation = if (isRolling) rollingRotation else 0f
    val currentScale = if (isRolling) rollingScale else 1f

        Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .scale(currentScale)
            .rotate(currentRotation)
            .shadow(
                elevation = if (enabled) 10.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Color(0x33000000)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                width = 2.dp,
                color = if (enabled) primaryColor.copy(alpha = 0.6f) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("dice_view")
    ) {
        // Render authentic dice pips
        DicePipsCanvas(
            value = diceValue.coerceIn(1, 6),
            pipColor = if (diceValue == 6) primaryColor else Color(0xFF0F172A),
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        )
    }
}

@Composable
private fun DicePipsCanvas(
    value: Int,
    pipColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val pipRadius = w * 0.11f

        // Standard dice grid coordinates (normalized 0.2, 0.5, 0.8)
        val left = w * 0.22f
        val centerX = w * 0.5f
        val right = w * 0.78f

        val top = h * 0.22f
        val centerY = h * 0.5f
        val bottom = h * 0.78f

        fun drawPip(x: Float, y: Float) {
            // Shadow behind pip for depth
            drawCircle(
                color = Color(0x33000000),
                radius = pipRadius * 1.15f,
                center = Offset(x + 1f, y + 1.5f)
            )
            drawCircle(
                color = pipColor,
                radius = pipRadius,
                center = Offset(x, y)
            )
            // Subtle highlight reflection
            drawCircle(
                color = Color(0x66FFFFFF),
                radius = pipRadius * 0.35f,
                center = Offset(x - pipRadius * 0.3f, y - pipRadius * 0.3f)
            )
        }

        when (value) {
            1 -> {
                drawPip(centerX, centerY)
            }
            2 -> {
                drawPip(left, top)
                drawPip(right, bottom)
            }
            3 -> {
                drawPip(left, top)
                drawPip(centerX, centerY)
                drawPip(right, bottom)
            }
            4 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
            5 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(centerX, centerY)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
            6 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(left, centerY)
                drawPip(right, centerY)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
        }
    }
}

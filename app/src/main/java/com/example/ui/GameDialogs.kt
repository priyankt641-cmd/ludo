package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.PlayerColor
import com.example.ui.theme.BrandOnPrimaryContainer
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryContainer

@Composable
fun ModeSelectDialog(
    currentMode: GameMode,
    currentAiCount: Int,
    onDismiss: () -> Unit,
    onSelectMode: (GameMode, Int) -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }
    var selectedAiCount by remember { mutableIntStateOf(currentAiCount) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandPrimaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = BrandOnPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Select Game Mode",
                        color = Color(0xFF0F172A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose players or vs Computer",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                GameMode.values().forEach { mode ->
                    val isSelected = (mode == selectedMode)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) BrandPrimaryContainer.copy(alpha = 0.35f) else Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) BrandPrimary else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedMode = mode }
                            .testTag("mode_card_${mode.name}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.title,
                                        color = if (isSelected) BrandPrimary else Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = mode.description,
                                        color = Color(0xFF64748B),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedMode = mode },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = BrandPrimary,
                                        unselectedColor = Color(0xFF94A3B8)
                                    )
                                )
                            }

                            // If Computer mode is selected, allow choosing number of bots
                            if (mode == GameMode.VS_COMPUTER && isSelected) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Opponents:",
                                    color = Color(0xFF475569),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    listOf(1 to "1 Bot", 2 to "2 Bots", 3 to "3 Bots").forEach { (count, label) ->
                                        FilterChip(
                                            selected = selectedAiCount == count,
                                            onClick = { selectedAiCount = count },
                                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BrandPrimary,
                                                selectedLabelColor = Color.White,
                                                containerColor = Color.White,
                                                labelColor = Color(0xFF475569)
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = if (selectedAiCount == count) BrandPrimary else Color(0xFFCBD5E1),
                                                selectedBorderColor = BrandPrimary,
                                                enabled = true,
                                                selected = selectedAiCount == count
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelectMode(selectedMode, selectedAiCount) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("start_game_mode_button")
            ) {
                Text("Start Game", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x33000000))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .testTag("mode_select_dialog")
    )
}

@Composable
fun RulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandPrimaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandOnPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Classic Ludo Rules",
                        color = Color(0xFF0F172A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Official international rules",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                RuleItem("🎯 Goal", "Be the first player to guide all 4 of your tokens safely around the track and into your home center triangle.")
                RuleItem("🎲 Roll a 6 to Exit Yard", "A token can only leave the base yard and enter the active track after rolling a 6.")
                RuleItem("⭐ Bonus Extra Rolls", "Rolling a 6, capturing an opponent's token, or bringing a token into the home center rewards you with an extra roll!")
                RuleItem("⚔️ Capturing Opponents", "If your token lands on an opponent's token on any standard track cell, the opponent's piece is knocked out and returned to its base yard.")
                RuleItem("🛡️ Safe Star Cells", "Cells marked with a Star (including start squares) are completely safe! Tokens resting on star cells cannot be captured.")
                RuleItem("🎯 Exact Roll to Finish", "Entering the center home requires the exact remaining dice count. Overshooting is not permitted.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("rules_ok_button")
            ) {
                Text("Got It!", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x33000000))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .testTag("rules_dialog")
    )
}

@Composable
private fun RuleItem(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(text = title, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(
            text = description,
            color = Color(0xFF475569),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun RestartConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFE4E6))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFFBE123C),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Restart Game?",
                    color = Color(0xFF0F172A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to start a new game? Current board progress will be reset.",
                color = Color(0xFF475569),
                fontSize = 13.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE123C)),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("confirm_restart_button")
            ) {
                Text("Restart", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x33000000))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .testTag("restart_confirm_dialog")
    )
}

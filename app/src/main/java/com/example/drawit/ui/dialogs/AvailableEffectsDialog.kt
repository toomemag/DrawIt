package com.example.drawit.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.drawit.painting.effects.BaseEffect
import com.example.drawit.ui.components.painting.EffectListItem

@Composable
fun AvailableEffectsDialog(
    availableEffects: List<BaseEffect<*>>,
    onEffectSelected: (effect: BaseEffect<*>) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(40.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            for ((index, effect) in availableEffects.withIndex()) {
                EffectListItem(
                    effect = effect,
                    onEffectClick = { onEffectSelected(effect) },
                    modifier = Modifier
                        .padding(bottom = if (index == availableEffects.size - 1) 8.dp else 0.dp)
                )
            }

            if (availableEffects.isEmpty()) {
                Text(
                    text="No effects available",
                    style=MaterialTheme.typography.bodyMedium,
                    color=MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
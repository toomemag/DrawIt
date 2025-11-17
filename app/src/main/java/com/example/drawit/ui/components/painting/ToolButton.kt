package com.example.drawit.ui.components.painting

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics


@Composable
fun ToolButton(
    onClick: ( ) -> Unit = { },
    isSelected: Boolean = false,
    icon: ImageVector,
    iconContentDescription: String = "unknown tool",
    iconModifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier
) {
    IconButton(
        modifier = buttonModifier.semantics {
            this.selected = isSelected
        },
        onClick = onClick,
        colors = IconButtonColors(
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.DarkGray
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = iconModifier
        )
    }
}
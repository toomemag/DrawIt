package com.example.drawit.ui.components.painting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.drawit.painting.effects.BaseEffect
import com.example.drawit.utils.darken


@Composable
fun EffectListItem(
    effect: BaseEffect<*>,
    onEffectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onEffectClick)
            .clip(RoundedCornerShape(10.dp))
            .background(darken(MaterialTheme.colorScheme.surface, .1f))
            .padding(10.dp)
            // only used for padding atm
            .then(modifier),
    ) {
        Text(
            text=effect.getEffectName(),
            style= MaterialTheme.typography.displaySmall,
            color= MaterialTheme.colorScheme.onSurface
        )

        Text(
            text=effect.getEffectDescription(),
            style= MaterialTheme.typography.bodyMedium,
            color= MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
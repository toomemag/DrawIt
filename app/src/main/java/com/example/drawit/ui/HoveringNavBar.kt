package com.example.drawit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drawit.ui.screens.Tab

@Composable
private fun NavItem(
    icon: ImageVector,
    isActive: Boolean,
    label: String,
    onClick: () -> Unit = { },
) {
    Column(
        modifier = Modifier
            .clickable {
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .size(30.dp),
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun HoveringNavBar(
    activeTab: Tab = Tab.Feed,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then( modifier ),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "Feed",
                isActive = activeTab == Tab.Feed,
                onClick = { onSelect(Tab.Feed) }
            )

            NavItem(
                icon = Icons.Default.Group,
                label = "Friends",
                isActive = activeTab == Tab.Friends,
                onClick = { onSelect(Tab.Friends) }
            )

            NavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isActive = activeTab == Tab.Profile,
                onClick = { onSelect(Tab.Profile) }
            )
        }
    }
}
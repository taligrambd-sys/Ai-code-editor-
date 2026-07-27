package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DeepNavyBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class StudioTab(val index: Int, val title: String, val icon: ImageVector) {
    object Chat : StudioTab(0, "Chat", Icons.Default.Forum)
    object Editor : StudioTab(1, "Editor", Icons.Default.Code)
    object Preview : StudioTab(2, "Preview", Icons.Default.Preview)
}

@Composable
fun StudioBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(StudioTab.Chat, StudioTab.Editor, StudioTab.Preview)

    NavigationBar(
        modifier = modifier,
        containerColor = DeepNavyBackground,
        tonalElevation = 8.dp
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab.index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.index) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DeepNavyBackground,
                    selectedTextColor = CyanPrimary,
                    indicatorColor = CyanPrimary,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

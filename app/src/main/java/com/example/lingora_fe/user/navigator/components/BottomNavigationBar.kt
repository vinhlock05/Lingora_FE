package com.example.lingora_fe.user.navigator.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.navigator.BottomNavItem

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Vocabulary,
        BottomNavItem.Practice,
        BottomNavItem.Materials,
        BottomNavItem.Dictionary,
        BottomNavItem.Forum,
        BottomNavItem.Profile
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val route = when (item) {
                BottomNavItem.Vocabulary -> Route.VocabularyTab.route
                BottomNavItem.Practice -> Route.PracticeTab.route
                BottomNavItem.Materials -> Route.MaterialsTab.route
                BottomNavItem.Dictionary -> Route.DictionaryTab.route
                BottomNavItem.Forum -> Route.ForumTab.route
                BottomNavItem.Profile -> Route.ProfileTab.route
            }
            BottomNavItemComponent(
                item = item,
                isSelected = currentRoute.startsWith(route),
                onClick = { onItemClick(route) }
            )
        }
    }
}

@Composable
private fun BottomNavItemComponent(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
    ) {
        Image(
            painter = painterResource(
                id = if (isSelected) item.selectedIcon else item.icon
            ),
            contentDescription = item.label,
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Fit
        )
        
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else NavBarText
        )
    }
}


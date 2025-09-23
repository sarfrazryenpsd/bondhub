package com.ryen.bondhub.presentation.screens.navBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun BottomNavItem(
    item: BottomNavItems,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (isSelected) Surface
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp)
        ,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                painter = painterResource(id = item.icon),
                contentDescription = item.title,
                tint = if (isSelected) Primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(if(item == BottomNavItems.Messages) 24.dp else 20.dp)
            )

            // Only show text if selected
            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun NavItemPrev() {
    BottomNavItem(
        item = BottomNavItems.Messages,
        isSelected = true,
        onClick = {}
    )
}
@Preview
@Composable
private fun NavItemPrev1() {
    BottomNavItem(
        item = BottomNavItems.FindFriends,
        isSelected = true,
        onClick = {}
    )
}
@Preview
@Composable
private fun NavItemPrev2() {
    BottomNavItem(
        item = BottomNavItems.FriendRequests,
        isSelected = true,
        onClick = {}
    )
}

@Preview
@Composable
private fun NavItemPrev3() {
    BottomNavItem(
        item = BottomNavItems.FriendRequests,
        isSelected = false,
        onClick = {}
    )
}
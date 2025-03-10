package com.ryen.bondhub.presentation.screens.navBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    val animatedWeight by animateFloatAsState(
        targetValue = if (isSelected) 1.5f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "weight animation"
    )

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (isSelected) Surface
                else Color.Transparent
            )
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clickable(onClick = onClick)
        ,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                painter = painterResource(id = item.icon),
                contentDescription = item.title,
                tint = if (isSelected) Primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            // Only show text if selected
            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.displayLarge,
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
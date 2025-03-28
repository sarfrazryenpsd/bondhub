package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun UserProfileTopAppBar(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary)
            .padding(8.dp)
            .padding(top = 24.dp)

    ){
        Box(
            modifier = Modifier.clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Surface,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = "Logout",
            color = Surface,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(end = 8.dp).clickable { onLogoutClick() }
        )
    }
}

@Preview
@Composable
private fun TopAppBarPrev() {
    UserProfileTopAppBar(
        onBackClick = {},
        onLogoutClick = {}
    )
}
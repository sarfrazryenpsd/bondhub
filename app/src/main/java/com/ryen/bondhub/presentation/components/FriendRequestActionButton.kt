package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Success
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun FriendRequestActionButton(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Success.copy(alpha = 0.2f))
                .border(1.5.dp, Success, CircleShape)
                .clickable { onAccept() }
        ) {
            Icon(
                painter = painterResource(R.drawable.mark),
                contentDescription = "Accept Request",
                tint = Success,
                modifier = Modifier.size(16.dp)
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(CircleShape)
                .size(30.dp)
                .background(Primary.copy(alpha = 0.2f))
                .border(1.5.dp, Primary, CircleShape)
                .clickable { onReject() }
        ) {
            Icon(
                painter = painterResource(R.drawable.close),
                contentDescription = "Accept Request",
                tint = Primary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Preview
@Composable
private fun RequestActionButtonPrev() {
    FriendRequestActionButton(
        onAccept = {},
        onReject = {}
    )
}
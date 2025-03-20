package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.R
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.presentation.theme.Pending
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Success
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun ConnectionActionButtons(
    connectionStatus: ConnectionStatus?,
    onSendRequest: (UserProfile) -> Unit,
    userProfile: UserProfile
) {
    when(connectionStatus){
        ConnectionStatus.PENDING -> {
            Row(
                modifier = Modifier
                    .clipToBounds()
                    .clip(RoundedCornerShape(50))
                    .background(Pending)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.time),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                        .size(18.dp),
                    tint = Surface
                )
                Text(
                    text = "Pending",
                    modifier = Modifier.padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Surface
                )
            }
        }
        ConnectionStatus.ACCEPTED -> {
            Row(
                modifier = Modifier
                    .clipToBounds()
                    .clip(RoundedCornerShape(50))
                    .background(Success)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.mark),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                        .size(18.dp),
                    tint = Surface
                )
                Text(
                    text = "Friends",
                    modifier = Modifier.padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Surface
                )
            }
        }
        else -> {
            Button(
                onClick = { onSendRequest(userProfile) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Surface
                ),
            ){
                Text("Add Friend")
            }
        }
    }
}

@Preview
@Composable
private fun FriendsPrev() {
    ConnectionActionButtons(
        connectionStatus = ConnectionStatus.ACCEPTED,
        onSendRequest = {},
        userProfile = UserProfile(
            displayName = "Aniyb",
            email = "asdubef@gmail.com",
        )
    )
}

@Preview
@Composable
private fun PendingPrev() {
    ConnectionActionButtons(
        connectionStatus = ConnectionStatus.PENDING,
        onSendRequest = {},
        userProfile = UserProfile(
            displayName = "Aniyb",
            email = "asdubef@gmail.com",
        )
    )
}

@Preview
@Composable
private fun AddPrev() {
    ConnectionActionButtons(
        connectionStatus = ConnectionStatus.INITIAL,
        onSendRequest = {},
        userProfile = UserProfile(
            displayName = "Aniyb",
            email = "asdubef@gmail.com",
        )
    )
}
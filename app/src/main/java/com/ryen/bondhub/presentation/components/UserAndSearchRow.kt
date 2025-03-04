package com.ryen.bondhub.presentation.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary


@Composable
fun ChatScreenUserAndSearchRow(
    context: Context,
    profilePictureUrl: String,
    displayName: String,
    messageMode: Boolean,
    lastMessage: String,
    lastMessageTime: String,
    unreadMessageCount: Int
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(profilePictureUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .clip(CircleShape)
                .size(64.dp)
                .border(4.dp, Color.Transparent, CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.userplaceholder),
            error = painterResource(R.drawable.userplaceholder)
        )
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = if(!messageMode) {
                    "Account info"
                } else {
                    if(lastMessage.length > 26){
                        lastMessage.take(26) + "..."
                    } else{
                        lastMessage
                    }
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Secondary.copy(alpha = 0.7f)
            )
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            if(!messageMode){
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Secondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else{
                Column(modifier = Modifier.height(48.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {
                    Text(
                        text = lastMessageTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = Secondary.copy(alpha = 0.7f)
                    )
                    if (unreadMessageCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Primary, CircleShape)
                                .align(Alignment.End),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadMessageCount.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenUserAndSearchRowPrev() {
    ChatScreenUserAndSearchRow(
        context = LocalContext.current,
        profilePictureUrl = "",
        displayName = "Sarfraz Ryen",
        messageMode = true,
        lastMessage = "Hello,ahowaareayou?hsadhlpdasdsad",
        lastMessageTime = "10:00 AM",
        unreadMessageCount = 5
    )
}
@Preview(showBackground = true)
@Composable
private fun ChatScreenUserAndSearchRowPrev2() {
    ChatScreenUserAndSearchRow(
        context = LocalContext.current,
        profilePictureUrl = "",
        displayName = "Sarfraz Ryen",
        messageMode = false,
        lastMessage = "",
        lastMessageTime = "10:00 AM",
        unreadMessageCount = 0
    )
}
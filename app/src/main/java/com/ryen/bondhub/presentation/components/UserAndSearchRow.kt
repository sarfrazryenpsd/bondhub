package com.ryen.bondhub.presentation.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.ryen.bondhub.domain.model.Chat
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary
import com.ryen.bondhub.util.formatTimestamp


@Composable
fun UserSearchAndMessageRow(
    context: Context,
    chat: Chat = Chat(),
    profilePictureUrl: String = "",
    displayName: String = "",
    messageMode: Boolean = false,
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onChatClick: (String, String) -> Unit = { _, _ -> }
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onChatClick(chat.chatId, chat.participants.last()) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(if(!messageMode) profilePictureUrl else chat.profilePictureUrlThumbnail)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .clip(CircleShape)
                .size(48.dp)
                .clickable {
                    if (messageMode) onChatClick(
                        chat.chatId,
                        chat.participants.last()
                    ) else onProfileClick()
                },
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.userplaceholder),
            error = painterResource(R.drawable.userplaceholder)
        )
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.clickable {
                if (messageMode) onChatClick(
                    chat.chatId,
                    chat.participants.last()
                ) else onProfileClick()
            }
        ) {
            Text(
                text = if(!messageMode) displayName else chat.displayName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = if(!messageMode) {
                    "Account info"
                } else {
                    if(chat.lastMessage != null){
                        if (chat.lastMessage.length > 26) chat.lastMessage.take(26) + "..." else chat.lastMessage
                    } else{
                        "No messages yet"
                    }
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if(!messageMode){
                    Secondary.copy(alpha = 0.7f)
                }else {
                    if (chat.unreadMessageCount > 0) Secondary else Secondary.copy(alpha = 0.7f)
                }
            )
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            if(!messageMode){
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Secondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.height(48.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatTimestamp(chat.lastMessageTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary.copy(alpha = 0.7f)
                    )

                    chat.unreadMessageCount.takeIf { it > 0 }?.let { count ->
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Primary, CircleShape)
                                .align(Alignment.End),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if(count > 99) "99+" else count.toString(),
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
private fun MessageRowPrev() {
    MaterialTheme {  // Or your custom theme
        UserSearchAndMessageRow(
            context = LocalContext.current,
            profilePictureUrl = "",
            displayName = "Sarfraz Ryen",
            messageMode = true,
            chat = Chat(
                chatId = "1",
                connectionId = "1",
                participants = listOf("user1", "user2"),
                profilePictureUrlThumbnail = "",
                displayName = "Sarfraz Ryen",
                lastMessage = "Hello, how are you? I'm under the water"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserAndSearchRowPrev() {
    MaterialTheme {  // Or your custom theme
        UserSearchAndMessageRow(
            context = LocalContext.current,
            profilePictureUrl = "",
            displayName = "Sarfraz Ryen",
            messageMode = false,
            chat = Chat()
        )
    }
}
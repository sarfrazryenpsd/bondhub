
package com.ryen.bondhub.presentation.contents

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.R
import com.ryen.bondhub.domain.model.Chat
import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.presentation.components.FriendsBottomSheet
import com.ryen.bondhub.presentation.components.SearchField
import com.ryen.bondhub.presentation.components.UserSearchAndMessageRow
import com.ryen.bondhub.presentation.state.ChatScreenState
import com.ryen.bondhub.presentation.state.FriendRequest
import com.ryen.bondhub.presentation.state.FriendsState
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun ChatScreenContent(
    displayName: String,
    profilePictureUrl: String,
    searchQuery: String,
    searchMode: Boolean,
    context: Context,
    onProfileClick: () -> Unit = {},
    friendsState: FriendsState,
    chatState: ChatScreenState,
    onSearchValueChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFriendsDismiss: () -> Unit,
    onMessageFABClick: () -> Unit,
    onFriendClick: (ChatConnection) -> Unit,
    onChatClick: (String, String) -> Unit = { _, _ -> },
    onDeleteChat: (String) -> Unit = {},
    paddingValues: PaddingValues,
) {
    var isSearchActive by remember { mutableStateOf(searchMode) }

    Column(
        modifier = Modifier
            .background(color = Color.LightGray)
            .padding(6.dp)
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Surface)
            .padding(horizontal = 18.dp, vertical = 22.dp),

    ){
        AnimatedContent(
            targetState = isSearchActive,
            label = "SearchTransition"
        ) { searchState ->
            if (!searchState) {
                UserSearchAndMessageRow(
                    context = context,
                    messageMode = false,
                    displayName = displayName,
                    profilePictureUrl = profilePictureUrl,
                    onSearchClick = { isSearchActive = true },
                    onProfileClick = onProfileClick,

                )
            } else {
                SearchField(
                    searchText = searchQuery,
                    onValueChange = { onSearchValueChange(it) },
                    onBackClick = { isSearchActive = false },
                    onSearchClick = { onSearchClick() },
                    searchChat = true
                )
            }
        }

        Text(
            text = "Message",
            color = Secondary.copy(alpha = 0.7f),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        when(chatState){
            is ChatScreenState.Loading -> {
                Box(modifier = Modifier.fillMaxSize()){
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(46.dp)
                            .align(Alignment.Center),
                        color = Primary,
                        trackColor = Primary.copy(alpha = .4f),
                        strokeWidth = 5.dp,
                    )
                }
            }
            is ChatScreenState.Success -> {
                val successState = chatState as ChatScreenState.Success
                if(successState.chats.isEmpty()){
                    Box(
                        modifier = Modifier
                            .padding(bottom = 120.dp)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Image(
                            painter = painterResource(id = R.drawable.no_messages),
                            contentDescription = "No Messages",
                            modifier = Modifier.size(300.dp)
                        )
                        Text(
                            text = "No messages yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Secondary.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 160.dp)
                        )

                    }
                } else{
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),

                    ) {
                        items(chatState.chats){ chat ->
                            UserSearchAndMessageRow(
                                context = context,
                                messageMode = true,
                                chat = chat,
                                onChatClick = onChatClick
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }
                if(successState.showFriendsBottomSheet){
                    FriendsBottomSheet(
                        friendsState = friendsState,
                        onDismiss = onFriendsDismiss,
                        onFriendClick = onFriendClick,
                    )
                }
            }
            is ChatScreenState.Error -> {
                val errorState = chatState as ChatScreenState.Error
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> { }
        }

    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd){
        FloatingActionButton(
            onClick = { onMessageFABClick() },
            containerColor = Primary,
            modifier = Modifier.padding(bottom = 110.dp, end = 26.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.start_message),
                contentDescription = "Start Chat",
                modifier = Modifier.size(30.dp),
                tint = Color.White
            )
        }
    }
}

@ExperimentalMaterial3Api
@Preview
@Composable
private fun ChatScreenContentPrev() {
    ChatScreenContent(
        displayName = "Sarfraz Ryen",
        profilePictureUrl = "",
        context = LocalContext.current,
        searchQuery = "kyaa",
        searchMode = false,
        paddingValues = PaddingValues(0.dp),
        onProfileClick = {},
        friendsState = FriendsState.Success(

            friends = listOf(
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),
                FriendRequest(
                    connection = ChatConnection(
                        connectionId = "1",
                        user1Id = "user1",
                        user2Id = "user2",
                    ),
                    senderProfile = UserProfile(
                        uid = "46841348",
                        displayName = "Aron Paul",
                        email = "william.@example.com"
                    )
                ),

                )
        ),
        onFriendsDismiss = {},
        onFriendClick = {},
        onMessageFABClick = {},
        chatState = ChatScreenState.Success(
            chats = emptyList(),
            showFriendsBottomSheet = true,
        ),
        onSearchValueChange = {},
        onSearchClick = {},
        onChatClick = { _, _ -> },
        onDeleteChat = {}
    )
}
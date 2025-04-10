package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.R
import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.presentation.state.FriendRequest
import com.ryen.bondhub.presentation.state.FriendsState
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsBottomSheet(
    friendsState: FriendsState,
    onDismiss: () -> Unit,
    onFriendClick: (ChatConnection) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = Surface,
        //scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            FriendsBottomSheetHeader(onDismiss)

            // Content based on friends state
            when (friendsState) {
                is FriendsState.Loading -> LoadingState()
                is FriendsState.Empty -> EmptyState()
                is FriendsState.Success -> SuccessState(
                    friends = friendsState.friends,
                    onFriendClick = onFriendClick
                )
                is FriendsState.Error -> ErrorState(message = friendsState.message)
            }
        }
    }
}

@Composable
private fun FriendsBottomSheetHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Friends",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium)
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close friends bottom sheet")
        }
    }
    HorizontalDivider()
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            color = Primary,
            trackColor = Primary.copy(alpha = 0.4f),
            strokeWidth = 4.dp
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You don't have any friends yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Go to Find Friends to connect with others",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SuccessState(
    friends: List<FriendRequest>,
    onFriendClick: (ChatConnection) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(friends) { friendRequest ->
            UserProfileCard(
                userProfile = friendRequest.senderProfile,
                actionContent = {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onFriendClick(friendRequest.connection) }
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.start_message),
                            contentDescription = "Start chat with ${friendRequest.senderProfile.displayName}",
                            tint = Surface,
                            modifier = Modifier.padding(8.dp).size(24.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun BottomSheetPrev() {
    FriendsBottomSheet(
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
        onDismiss = {},
        onFriendClick = {}
    )
}

@Preview
@Composable
private fun BottomSheetPrev2() {
    FriendsBottomSheet(
        friendsState = FriendsState.Loading,
        onDismiss = {},
        onFriendClick = {}
    )
}

@Preview
@Composable
private fun BottomSheetPrev3() {
    FriendsBottomSheet(
        friendsState = FriendsState.Empty,
        onDismiss = {},
        onFriendClick = {}
    )
}
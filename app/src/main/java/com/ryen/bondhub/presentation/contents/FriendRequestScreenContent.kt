package com.ryen.bondhub.presentation.contents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.R
import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.presentation.components.FriendRequestActionButton
import com.ryen.bondhub.presentation.components.UserProfileCard
import com.ryen.bondhub.presentation.state.FriendRequest
import com.ryen.bondhub.presentation.state.FriendRequestsState
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun FriendRequestScreenContent(
    paddingValues: PaddingValues,
    state: FriendRequestsState,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
            .clip(RoundedCornerShape(32.dp))
            .padding(6.dp)
            .background(color = Surface)
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp, top = 46.dp)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        when(state){
            is FriendRequestsState.Loading, is FriendRequestsState.Initial -> {
                item{
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(38.dp),
                        color = Primary,
                        trackColor = Primary.copy(alpha = .4f),
                        strokeWidth = 5.dp,
                    )
                }
            }

            is FriendRequestsState.Error, is FriendRequestsState.Empty -> {
                item{
                    Image(
                        painter = painterResource(R.drawable.friend_request_svg),
                        contentDescription = "Find Friends",
                        modifier = Modifier
                            .padding(top = 200.dp)
                            .size(250.dp)
                    )
                }
                item {
                    Text(
                        text = "No friend requests",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            is FriendRequestsState.Success -> {
                val requests = (state as FriendRequestsState.Success).requests
                items(requests) { request ->
                    UserProfileCard(
                        userProfile = request.senderProfile,
                        actionContent = {
                            FriendRequestActionButton(
                                onAccept = { onAccept(request.connection.connectionId) },
                                onReject = { onReject(request.connection.connectionId) }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun FriendReqEmptyPrev()  {
    FriendRequestScreenContent(
        paddingValues = PaddingValues(0.dp),
        state = FriendRequestsState.Empty,
        onAccept = {},
        onReject = {}
    )
}

@Preview
@Composable
private fun FriendReqFullPrev() {
    val mockUserProfile = UserProfile(
        uid = "user1",
        displayName = "John Doe",
        email = "john.doe@example.com",
    )

    val mockConnection = ChatConnection(
        user1Id = "user1",
        user2Id = "user2",
        initiatorId = "user1"
    )
    val mockRequests = listOf(
        FriendRequest(
        connection = mockConnection,
        senderProfile = mockUserProfile
        ),
        FriendRequest(
            connection = mockConnection.copy(
                user1Id = "user2",
                user2Id = "user3"
            ),
            senderProfile = mockUserProfile.copy(
                uid = "user2",
                displayName = "Jane Smith",
                email = "jane.smith@example.com"
            )
        ),
        FriendRequest(
            connection = mockConnection.copy(
                user1Id = "user3",
                user2Id = "user4"
            ),
            senderProfile = mockUserProfile.copy(
                uid = "user3",
                displayName = "Bob Johnson",
                email = "bob.jognson@example.com"
            )
        )
    )
    FriendRequestScreenContent(
        paddingValues = PaddingValues(0.dp),
        state = FriendRequestsState.Success(mockRequests),
        onAccept = {},
        onReject = {}
    )
}
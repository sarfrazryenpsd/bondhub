package com.ryen.bondhub.presentation.contents

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.R
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.presentation.components.ConnectionActionButtons
import com.ryen.bondhub.presentation.components.SearchField
import com.ryen.bondhub.presentation.components.UserProfileCard
import com.ryen.bondhub.presentation.state.FindFriendsState
import com.ryen.bondhub.presentation.theme.Error
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun FindFriendsScreenContent(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    uiState: FindFriendsState,
    paddingValues: PaddingValues,
    onSendRequest: (UserProfile) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
            .clip(RoundedCornerShape(32.dp))
            .padding(6.dp)
            .background(color = Surface)
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp, top = 46.dp)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchField(
            searchText = query,
            onValueChange = onQueryChanged,
            onBackClick = {},
            onSearchClick = onSearch,
            searchChat = false
        )
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedVisibility(
            visible = query.isNotBlank(),
            enter = slideInVertically { height -> height },
            exit = slideOutVertically { height -> height }
        ) {

            Box(
                modifier = Modifier
                    .clipToBounds()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            ){
                if(!Patterns.EMAIL_ADDRESS.matcher(query).matches()){
                    Text(
                        text = "Invalid Email",
                        textAlign = TextAlign.Start,
                        color = Error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    )
                } else {
                    when (uiState){
                        is FindFriendsState.Loading -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                    //.align(Alignment.Center),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White,
                                )
                            ){
                                Box(contentAlignment = Alignment.Center, modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                                    .clipToBounds()){
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .align(Alignment.Center),
                                        color = Primary,
                                        trackColor = Primary.copy(alpha = .4f),
                                        strokeWidth = 5.dp,
                                    )
                                }
                            }
                        }
                        is FindFriendsState.UserFound -> {
                            UserProfileCard(
                                userProfile = uiState.userProfile,
                                actionContent = {
                                    ConnectionActionButtons(
                                        userProfile = uiState.userProfile,
                                        onSendRequest = { onSendRequest(uiState.userProfile) },
                                        connectionStatus = uiState.connectionStatus
                                    )
                                }
                            )
                        }
                        is FindFriendsState.UserNotFound -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                                    .align(Alignment.Center),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White,
                                )
                            ){
                                Text(
                                    text = "User not found",
                                    textAlign = TextAlign.Center,
                                    color = Error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 12.dp)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(R.drawable.findfriends),
            contentDescription = "Find Friends",
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center),
        )
    }
}

@Preview
@Composable
private fun FindFriendsScreenContentPrev() {
    FindFriendsScreenContent(
        query = "asdubef@gmail.com",
        onQueryChanged = {},
        onSearch = {},
        uiState = FindFriendsState.UserFound(
            UserProfile(
                displayName = "Aniyb",
                email = "asdubef@gmail.com",
                bio = "inih ihi"
            ),
            connectionStatus = null
        ),
        paddingValues = PaddingValues(0.dp)
    )
}
@Preview
@Composable
private fun FindFriendsScreenContentPrev1() {
    FindFriendsScreenContent(
        query = "asdubef@gmail.com",
        onQueryChanged = {},
        onSearch = {},
        uiState = FindFriendsState.Loading,
        paddingValues = PaddingValues(0.dp)
    )
}
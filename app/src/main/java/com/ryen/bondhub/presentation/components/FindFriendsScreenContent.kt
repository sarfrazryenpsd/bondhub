package com.ryen.bondhub.presentation.components

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onSendRequest: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .background(color = Color.LightGray)
            .padding(paddingValues)
            .padding(6.dp)
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Surface)
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
                            CircularProgressIndicator(
                                modifier = Modifier.width(56.dp),
                                color = Primary,
                                trackColor = Primary.copy(alpha = .4f),
                                strokeWidth = 5.dp,
                            )
                        }
                        is FindFriendsState.UserFound -> {
                            UserProfileCard(
                                userProfile = uiState.userProfile,
                                onSendRequest = { onSendRequest(uiState.userProfile.uid) }
                            )
                        }
                        is FindFriendsState.UserNotFound -> {
                            Text(
                                text = "User not found",
                                textAlign = TextAlign.Start,
                                color = Error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            )
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
            )
        ),
        paddingValues = PaddingValues(0.dp)
    )
}
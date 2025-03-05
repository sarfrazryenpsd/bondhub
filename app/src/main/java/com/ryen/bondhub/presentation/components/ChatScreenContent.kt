
package com.ryen.bondhub.presentation.components

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.presentation.theme.Secondary
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun ChatScreenContent(
    displayName: String,
    lastMessage: String,
    profilePictureUrl: String,
    searchQuery: String,
    searchMode: Boolean,
    context: Context,
    onProfileClick: () -> Unit = {},
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
                    lastMessage = lastMessage
                )
            } else {
                SearchField(
                    searchText = searchQuery,
                    onValueChange = {},
                    onBackClick = { isSearchActive = false },
                    onSearchClick = {}
                )
            }
        }

        Text(
            text = "Message",
            color = Secondary.copy(alpha = 0.7f),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Preview
@Composable
private fun ChatScreenContentPrev() {
    ChatScreenContent(
        displayName = "Sarfraz Ryen",
        profilePictureUrl = "",
        context = LocalContext.current,
        searchQuery = "kyaa",
        searchMode = false,
        onProfileClick = {},
        lastMessage = ""
    )
}
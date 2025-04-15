package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ryen.bondhub.R
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMessageTopBar(
    onBackClick: () -> Unit,
    userProfile: UserProfile? = null,
    isOnline: Boolean = false
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Profile Image or Placeholder
                AsyncImage(
                    model = userProfile?.profilePictureThumbnailUrl ?: R.drawable.userplaceholder,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Column {
                    Text(
                        text = userProfile?.displayName ?: "Chat",
                        style = MaterialTheme.typography.titleMedium,
                        overflow = TextOverflow.Ellipsis,
                        color = Surface,
                        maxLines = 1
                    )
                    if (isOnline) {
                        Text(
                            text = "Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Surface)
            }
        },
        actions = {
            IconButton(onClick = { /* Will implement later */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Surface)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Primary
        )
    )
}

@Preview
@Composable
private fun ChatMessageTopBarPrev() {
    ChatMessageTopBar(
        onBackClick = {},
        userProfile = UserProfile(
            displayName = "John Doe",
            profilePictureUrl = null
        ),
    )
}
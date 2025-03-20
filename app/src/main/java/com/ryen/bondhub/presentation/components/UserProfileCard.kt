package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ryen.bondhub.R
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.model.UserStatus
import com.ryen.bondhub.presentation.theme.BondHubTheme
import com.ryen.bondhub.presentation.theme.Secondary

@Composable
fun UserProfileCard(
    userProfile: UserProfile,
    actionContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Profile Image
            AsyncImage(
                model = userProfile.profilePictureThumbnailUrl ?: R.drawable.userplaceholder,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // User Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userProfile.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                )
                Text(
                    text = userProfile.email,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action content slot
            actionContent()
        }
    }
}

@Preview
@Composable
fun UserProfileCardWithConnectionActionsPreview() {
    // Create a sample UserProfile for the preview
    val sampleUserProfile = UserProfile(
        uid = "sample-uid",
        displayName = "Jane Doe",
        email = "jane.doe@example.com",
        profilePictureUrl = null, // Will use placeholder
        bio = "Sample bio text",
        status = UserStatus.ONLINE
    )

    // Create a themed preview
    BondHubTheme  {
        // Preview different connection statuses
        Column {
            // None status
            UserProfileCard(
                userProfile = sampleUserProfile,
                actionContent = {
                    ConnectionActionButtons(
                        connectionStatus = null,
                        onSendRequest = { /* No-op for preview */ },
                        userProfile = sampleUserProfile
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pending status
            UserProfileCard(
                userProfile = sampleUserProfile,
                actionContent = {
                    ConnectionActionButtons(
                        connectionStatus = ConnectionStatus.PENDING,
                        onSendRequest = { /* No-op for preview */ },
                        userProfile = sampleUserProfile
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Accepted status
            UserProfileCard(
                userProfile = sampleUserProfile,
                actionContent = {
                    ConnectionActionButtons(
                        connectionStatus = ConnectionStatus.ACCEPTED,
                        onSendRequest = { /* No-op for preview */ },
                        userProfile = sampleUserProfile
                    )
                }
            )
        }
    }
}
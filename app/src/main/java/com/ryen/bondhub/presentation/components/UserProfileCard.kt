package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.ryen.bondhub.presentation.theme.Secondary
import java.util.UUID

@Composable
fun UserProfileCard(
    userProfile: UserProfile,
    onSendRequest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            AsyncImage(
                model = userProfile.profilePictureThumbnailUrl ?: R.drawable.userplaceholder,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
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
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = userProfile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Secondary
                )
                if (userProfile.bio.isNotEmpty()) {
                    Text(
                        text = userProfile.bio,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Send Request Button
            Button(
                onClick = onSendRequest,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = "Add")
            }
        }
    }
}

@Preview
@Composable
private fun UserProfileCardPreview() {
    UserProfileCard(
        userProfile = UserProfile(
            uid = UUID.randomUUID().toString(),
            email = "james.iredell@examplepetstore.com",
            displayName = "John Doe",
            bio = "This is a sample bio."
        ),
        onSendRequest = {}

    )
}
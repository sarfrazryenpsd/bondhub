package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ryen.bondhub.R
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.presentation.theme.Pending
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary
import com.ryen.bondhub.presentation.theme.Success
import com.ryen.bondhub.presentation.theme.Surface
import java.util.UUID

@Composable
fun UserProfileCard(
    userProfile: UserProfile,
    connectionStatus: ConnectionStatus? = null,
    onSendRequest: () -> Unit
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
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = userProfile.email,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Send Request Button
            when(connectionStatus){
                ConnectionStatus.PENDING -> {
                    Row(
                        modifier = Modifier
                            .clipToBounds()
                            .clip(RoundedCornerShape(50))
                            .background(Pending)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    )
                    {
                        Icon(
                            painter = painterResource(R.drawable.time),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp).size(18.dp),
                            tint = Surface
                        )
                        Text(
                            text = "Pending",
                            modifier = Modifier.padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = Surface
                        )
                    }
                }
                ConnectionStatus.ACCEPTED -> {
                    Row(
                        modifier = Modifier
                            .clipToBounds()
                            .clip(RoundedCornerShape(50))
                            .background(Success)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                        )
                    {
                        Icon(
                            painter = painterResource(R.drawable.mark),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp).size(18.dp),
                            tint = Surface
                        )
                        Text(
                            text = "Friends",
                            modifier = Modifier.padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = Surface
                        )
                    }
                }
                else -> {
                    Button(
                        onClick = onSendRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Surface
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ){
                        Text("Add Friend")
                    }
                }
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
            email = "james@example.com",
            displayName = "John Doe",
            bio = "This is a sample bio."
        ),
        connectionStatus = ConnectionStatus.PENDING,
        onSendRequest = {}

    )
}
@Preview
@Composable
private fun UserProfileCardPreview1() {
    UserProfileCard(
        userProfile = UserProfile(
            uid = UUID.randomUUID().toString(),
            email = "james@example.com",
            displayName = "John Doe",
            bio = "This is a sample bio."
        ),
        connectionStatus = ConnectionStatus.ACCEPTED,
        onSendRequest = {}

    )
}
@Preview
@Composable
private fun UserProfileCardPreview2() {
    UserProfileCard(
        userProfile = UserProfile(
            uid = UUID.randomUUID().toString(),
            email = "james@example.com",
            displayName = "John Doe",
            bio = "This is a sample bio."
        ),
        connectionStatus = null,
        onSendRequest = {}

    )
}
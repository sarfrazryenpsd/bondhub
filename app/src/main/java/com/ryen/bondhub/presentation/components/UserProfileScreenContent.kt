package com.ryen.bondhub.presentation.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary
import com.ryen.bondhub.presentation.theme.Surface


@Composable
fun ProfileUpdateScreenContent(
    email: String,
    profilePictureUrl: String?,
    displayName: String,
    bio: String,
    onDisplayNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onEditProfilePictureClick: () -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit,
    context: Context,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface)
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .padding(top = 65.dp, bottom = 20.dp)
                        .size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .size(120.dp)
                            .border(2.2.dp, Secondary, CircleShape)
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(profilePictureUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(110.dp)
                            .border(4.dp, Color.Transparent, CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.userplaceholder),
                        error = painterResource(R.drawable.userplaceholder)
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onEditProfilePictureClick() }
                            .background(Primary)
                            .align(Alignment.BottomEnd)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.edit_icon),
                            contentDescription = "Edit Profile Picture",
                            colorFilter = ColorFilter.tint(Secondary),
                            modifier = Modifier
                                .size(21.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
                Text(
                    text = email,
                    color = Secondary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { onDisplayNameChange(it.trim()) },
                    label = { Text("Name", color = Secondary.copy(alpha = .5f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Secondary,
                        unfocusedTextColor = Secondary,
                        focusedContainerColor = Primary.copy(alpha = .4f),
                        unfocusedContainerColor = Primary.copy(alpha = .2f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { onBioChange(it.trim()) },
                    label = { Text("Bio", color = Secondary.copy(alpha = .5f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Secondary,
                        unfocusedTextColor = Secondary,
                        focusedContainerColor = Primary.copy(alpha = .4f),
                        unfocusedContainerColor = Primary.copy(alpha = .2f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        unfocusedLabelColor = Secondary,
                        focusedLabelColor = Secondary
                    )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { onSkip() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary)
                ) { Text("Skip") }
                Button(
                    onClick = { onSave() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Save") }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ProfileUpdateScreenContentPreview() {
    ProfileUpdateScreenContent(
        email = "user@example.com",
        profilePictureUrl = null,
        displayName = "John Doe",
        bio = "Software Developer",
        onDisplayNameChange = {},
        onBioChange = {},
        onEditProfilePictureClick = {},
        onSkip = {},
        onSave = {},
        context = LocalContext.current,
    )
}
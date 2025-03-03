package com.ryen.bondhub.presentation.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary

//SEPERATE MESSAGEROW TO REUSES

@Composable
fun ChatScreenUserAndSearchRow(
    context: Context,
    profilePictureUrl: String,
    displayName: String,
    searchMode: Boolean,
    searchText: String,
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        AnimatedVisibility(!searchMode){
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(profilePictureUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(64.dp)
                    .border(4.dp, Color.Transparent, CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.userplaceholder),
                error = painterResource(R.drawable.userplaceholder)
            )
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Account info",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Secondary.copy(alpha = 0.7f)
                )
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Secondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        AnimatedVisibility(searchMode){
            TextField(
                value = searchText,
                onValueChange = {

                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            width = 4.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Primary,
                                    Primary.copy(alpha = 0.2f)
                                )
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenUserAndSearchRowPrev() {
    ChatScreenUserAndSearchRow(
        context = LocalContext.current,
        profilePictureUrl = "",
        displayName = "Sarfraz Ryen",
        searchMode = false,
        searchText = "Kyaaa"
    )
}
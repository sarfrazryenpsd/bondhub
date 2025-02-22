package com.ryen.bondhub.presentation.screens.chat

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary
import com.ryen.bondhub.presentation.theme.Surface



@Composable
fun ProfileUpdateScreen(
    onDone: () -> Unit = {},
    onSkip: () -> Unit = {}
) {

    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(modifier = Modifier
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
                    Image(
                        painter = painterResource(R.drawable.userplaceholder),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(110.dp)
                            .border(4.dp, Color.Transparent, CircleShape)
                    )
                    Box(modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { /*TODO*/ }
                        .background(Primary)
                        .align(Alignment.BottomEnd)
                        //.border(.3.dp, Surface, CircleShape)
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
                    text = "user@mail.com",
                    color = Secondary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it.trim() },
                    label = { Text("Name", color = Secondary.copy(alpha = .5f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Secondary,
                        unfocusedTextColor = Secondary,
                        focusedContainerColor = Primary.copy(alpha = .4f),
                        unfocusedContainerColor = Primary.copy(alpha = .2f),
                        focusedIndicatorColor = Color.Transparent,
                    )
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { bio = it.trim() },
                    label = { Text("Bio", color = Secondary.copy(alpha = .5f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Secondary,
                        unfocusedTextColor = Secondary,
                        focusedContainerColor = Primary.copy(alpha = .4f),
                        unfocusedContainerColor = Primary.copy(alpha = .2f),
                        focusedIndicatorColor = Color.Transparent,
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
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                )
                { Text("Save") }
            }
        }
    }
}

@Preview
@Composable
private fun UserProfileScreenPreview() {
    ProfileUpdateScreen()
}
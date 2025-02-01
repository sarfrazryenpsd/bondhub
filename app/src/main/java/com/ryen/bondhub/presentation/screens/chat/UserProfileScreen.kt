package com.ryen.bondhub.presentation.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.theme.Secondary
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun UserProfileScreen() {

    var displayName by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 65.dp, bottom = 20.dp)
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ){
                Image(
                    painter = painterResource(R.drawable.userplaceholder),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.clip(CircleShape)
                )
                Box(modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(Surface)
                    .align(Alignment.BottomEnd)
                ){
                    Image(
                        painter = painterResource(R.drawable.edit_icon),
                        contentDescription = "Edit Profile Picture",
                        colorFilter = ColorFilter.tint(Secondary)
                    )
                }
            }
            TextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Secondary,
                    unfocusedTextColor = Secondary,
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    focusedIndicatorColor = Secondary,
                ),
                modifier = Modifier
            )
        }
    }
}

@Preview
@Composable
private fun UserProfileScreenPreview() {
    UserProfileScreen()
}
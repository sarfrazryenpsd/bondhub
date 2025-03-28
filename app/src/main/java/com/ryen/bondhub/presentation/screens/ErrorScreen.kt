package com.ryen.bondhub.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.presentation.theme.BondHubTheme
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun ErrorScreen(
    message: String?,
    onRetry: () -> Unit
) {
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface)
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = message ?: "Something went wrong")
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Surface
                )

            ) {
                Text(text = "Retry")
            }
        }
    }
}

@Preview
@Composable
private fun ErrorScreenPrev() {
    BondHubTheme {
        ErrorScreen(
            message = "Something went wrong",
            onRetry = {}
        )
    }
}
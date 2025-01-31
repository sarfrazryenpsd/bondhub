package com.ryen.bondhub.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.theme.BondHubTheme
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun LoadingScreen() {
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface)
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

@Preview
@Composable
private fun LoadingScreenPrev() {
    BondHubTheme {
        LoadingScreen()
    }
}
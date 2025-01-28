package com.ryen.bondhub.presentation.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.event.AuthEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.AuthState
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary
import com.ryen.bondhub.presentation.theme.Surface
import com.ryen.bondhub.presentation.theme.Tertiary

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToChat: () -> Unit
) {

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect{event ->
            when(event){
                is UiEvent.NavigateToChat -> onNavigateToChat()
            }
        }
    }

    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var signInState by remember { mutableStateOf(true) }


    Scaffold(modifier = Modifier
        .fillMaxSize()
        .background(Surface)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp)
                .background(color = Surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logotype),
                contentDescription = null,
                modifier = Modifier.size(240.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row{
                Text(
                    text = "Sign ",
                    color = Tertiary,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Medium)
                )
                AnimatedContent(
                    targetState = signInState,
                    transitionSpec = {
                        if (targetState) {
                            // Slide in from bottom, slide out to top
                            slideInVertically { height -> height } togetherWith
                                    slideOutVertically { height -> -height }
                        } else {
                            // Slide in from bottom, slide out to top
                            slideInVertically { height -> -height } togetherWith
                                    slideOutVertically { height -> height }
                        }
                    }
                ) { targetState ->
                    when (targetState) {
                        true -> Text(
                            text = "In",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Medium),
                            color = Tertiary
                        )

                        false -> Text(
                            text = "Up",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Medium),
                            color = Tertiary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedVisibility(!signInState){
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)

                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = {email = it},
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = {password = it},
                label = { Text("Password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    if(password.isNotEmpty()){
                        IconButton(onClick = { visibility = !visibility }) {
                            Icon(
                                painter = if (visibility) painterResource(R.drawable.visibilityonn) else painterResource(R.drawable.visibilityoff),
                                tint = Color.Black.copy(alpha = .6f),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            )
            AnimatedVisibility(!signInState){
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        if (password.isNotEmpty()) {
                            IconButton(onClick = { visibility = !visibility }) {
                                if (visibility) {
                                    Icon(
                                        painter = painterResource(R.drawable.visibilityonn),
                                        tint = Color.Black.copy(alpha = .6f),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.visibilityoff),
                                        tint = Color.Black.copy(alpha = .6f),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    },
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    if(signInState){
                        viewModel.onEvent(AuthEvent.SignIn(email, password))
                    }else{
                        viewModel.onEvent(AuthEvent.SignUp(email, password, fullName))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = signInState ||
                        (password.isNotEmpty() &&
                        confirmPassword.isNotEmpty() &&
                        email.isNotEmpty() && fullName.isNotEmpty() &&
                        (password == confirmPassword)),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text(
                text = if (signInState) "Login" else "Sign Up",
                style = MaterialTheme.typography.labelLarge
            ) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                val text = if(signInState) "Don't" else "Already"
                Text(
                    text = "$text have an account?",
                    color = Secondary,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = if(!signInState)"Sign in" else "Sign up",
                    color = Primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { signInState = !signInState }
                )
            }
            when(authState){
                is AuthState.Loading -> CircularProgressIndicator()
                is AuthState.Error -> Text(text = (authState as AuthState.Error).message)
                is AuthState.Authenticated -> { onNavigateToChat() }
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    AuthScreen( onNavigateToChat = {})
}
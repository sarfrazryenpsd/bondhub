package com.ryen.bondhub.presentation.components

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary
import com.ryen.bondhub.presentation.theme.Surface
import com.ryen.bondhub.presentation.theme.Tertiary
import com.ryen.bondhub.util.ValidationResult

@Composable
fun AuthScreenContent(
    email: String,
    fullName: String,
    password: String,
    confirmPassword: String,
    passwordVisibility: Boolean,
    signInState: Boolean,
    onEmailChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onVisibilityChange: (Boolean) -> Unit,
    onSignInStateChange: (Boolean) -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onEmailCheck: (String) -> ValidationResult,
    onFullNameCheck: (String) -> ValidationResult,
    onPasswordCheck: (String) -> ValidationResult,
    onConfirmPasswordCheck: (String, String) -> ValidationResult,
    paddingValues: PaddingValues
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp)
                .background(color = Surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(signInState){
                Image(
                    painter = painterResource(R.drawable.logofull),
                    contentDescription = null,
                    modifier = Modifier.padding(top = 40.dp).size(240.dp)
                )
            }
            AnimatedVisibility(!signInState) {
                Image(
                    painter = painterResource(R.drawable.logotype),
                    contentDescription = null,
                    modifier = Modifier.size(240.dp)
                )
            }
            Spacer(modifier = Modifier.height(if (signInState) 20.dp else 0.dp))
            Row {
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
            AnimatedVisibility(!signInState) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Full Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    supportingText = {
                        if(fullName.isNotEmpty() && !signInState){
                            when(val result = onFullNameCheck(fullName)){
                                is ValidationResult.Error -> Text(text = result.message, color = Color.Red)
                                else -> {}

                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Face,
                            contentDescription = "name",
                            tint = Secondary.copy(alpha = .7f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    if(email.isNotEmpty() && !signInState){
                        when(val result = onEmailCheck(email)){
                            is ValidationResult.Error -> Text(text = result.message, color = Color.Red)
                            else -> {}

                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.MailOutline,
                        contentDescription = "email",
                        tint = Secondary.copy(alpha = .7f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = if (signInState) ImeAction.Done else ImeAction.Next
                ),
                supportingText = {
                        if(password.isNotEmpty() && !signInState){
                        when(val result = onPasswordCheck(password)){
                            is ValidationResult.Error -> Text(text = result.message, color = Color.Red)
                            else -> {}

                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.password),
                        contentDescription = "email",
                        tint = Secondary.copy(alpha = .7f),
                        modifier = Modifier.size(24.dp)
                    )
                },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    if (password.isNotEmpty()) {
                        IconButton(onClick = { onVisibilityChange(!passwordVisibility) }) {
                            Icon(
                                painter = if (passwordVisibility) painterResource(R.drawable.visibilityonn) else painterResource(
                                    R.drawable.visibilityoff
                                ),
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
            AnimatedVisibility(!signInState) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    supportingText = {
                        if(confirmPassword.isNotEmpty() && password.isNotEmpty() && !signInState){
                            when(val result = onConfirmPasswordCheck(password, confirmPassword)){
                                is ValidationResult.Error -> Text(text = result.message, color = Color.Red)
                                else -> {}

                            }
                        }
                    },
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        if (password.isNotEmpty()) {
                            IconButton(onClick = { onVisibilityChange(!passwordVisibility) }) {
                                if (passwordVisibility) {
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
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.password),
                            contentDescription = "email",
                            tint = Secondary.copy(alpha = .7f),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = if (signInState) onSignInClick else onSignUpClick,
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
            ) {
                val text = if (signInState) "Don't" else "Already"
                Text(
                    text = "$text have an account?",
                    color = Secondary,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = if (!signInState) "Sign in" else "Sign up",
                    color = Primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { onSignInStateChange(!signInState) }
                )
            }
        }

}

@Preview
@Composable
private fun AuthScreenSignInPrev() {
    AuthScreenContent(
        email = "test@example.com",
        fullName = "",
        password = "password123",
        confirmPassword = "password123",
        passwordVisibility = false,
        signInState = true,
        onEmailChange = {},
        onFullNameChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onVisibilityChange = {},
        onSignInStateChange = {},
        onSignInClick = {},
        onSignUpClick = {},
        paddingValues = PaddingValues(),
        onEmailCheck = { ValidationResult.Error("Invalid email format") },
        onFullNameCheck = { ValidationResult.Error("Name cannot be empty") },
        onPasswordCheck = { ValidationResult.Error("Password cannot be empty") },
        onConfirmPasswordCheck = { _, _ ->ValidationResult.Error("Passwords do not match")}
    )
}
@Preview
@Composable
private fun AuthScreenSignUpPrev() {
    AuthScreenContent(
        email = "test@example.com",
        fullName = "John Doe",
        password = "password123",
        confirmPassword = "password123",
        passwordVisibility = false,
        signInState = false,
        onEmailChange = {},
        onFullNameChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onVisibilityChange = {},
        onSignInStateChange = {},
        onSignInClick = {},
        onSignUpClick = {},
        paddingValues = PaddingValues(),
        onEmailCheck = { ValidationResult.Error("Invalid email format") },
        onFullNameCheck = { ValidationResult.Error("Name cannot be empty") },
        onPasswordCheck = { ValidationResult.Error("Password cannot be empty") },
        onConfirmPasswordCheck = { _, _ -> ValidationResult.Error("Passwords do not match")}
    )
}
package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.presentation.theme.Error
import com.ryen.bondhub.presentation.theme.Secondary
import com.ryen.bondhub.presentation.theme.Success
import com.ryen.bondhub.presentation.theme.Surface

@Composable
fun CustomSnackbar(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    snackBarState: SnackBarState
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { data ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                content = {
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                containerColor = if (snackBarState == SnackBarState.SUCCESS) Success else Error,
                shape = RoundedCornerShape(8.dp),
                action = data.visuals.actionLabel?.let {
                    {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = it, // Use the action label here
                                color = if (snackBarState == SnackBarState.SUCCESS) Surface else Secondary,
                            )
                        }
                    }
                }
            )
        }
    )
}

@Composable
@Preview(showBackground = true)
private fun SnackbarPrev() {
    val snackbarHostState = remember { SnackbarHostState() }

    // Simulate showing a Snackbar
    LaunchedEffect(Unit) {
        snackbarHostState.showSnackbar(
            message = "This is a custom Snackbar",
            actionLabel = "Dismiss",
            duration = SnackbarDuration.Short
        )
    }

    CustomSnackbar(
        snackbarHostState = snackbarHostState,
        onDismiss = {},
        snackBarState = SnackBarState.SUCCESS
    )
}
@Composable
@Preview(showBackground = true)
private fun SnackbarPrev1() {
    val snackbarHostState = remember { SnackbarHostState() }

    // Simulate showing a Snackbar
    LaunchedEffect(Unit) {
        snackbarHostState.showSnackbar(
            message = "This is a custom Snackbar",
            actionLabel = "Dismiss",
            duration = SnackbarDuration.Short
        )
    }

    CustomSnackbar(
        snackbarHostState = snackbarHostState,
        onDismiss = {},
        snackBarState = SnackBarState.ERROR
    )
}

enum class SnackBarState{
    SUCCESS, ERROR, INITIAL
}
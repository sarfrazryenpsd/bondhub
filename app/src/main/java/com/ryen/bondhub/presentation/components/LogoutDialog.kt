package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ryen.bondhub.presentation.theme.Error

@Composable
fun LogoutDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon", tint = Error, modifier = Modifier.size(48.dp))
        },
        title = {
            Text(text = dialogTitle, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Text(text = dialogText, style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp))
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmation()
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = Error,
                    contentColor = Color.White
                )
            ) {
                Text("Erase data")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss", color = Color.White)
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}
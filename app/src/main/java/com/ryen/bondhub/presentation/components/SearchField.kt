package com.ryen.bondhub.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.presentation.theme.Primary

@Composable
fun SearchField(
    searchText: String,
) {
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
                focusedIndicatorColor = Primary,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            )
        )
}

@Preview
@Composable
private fun SearchFieldPrev() {
    SearchField(
        searchText = "kyya",
    )
}
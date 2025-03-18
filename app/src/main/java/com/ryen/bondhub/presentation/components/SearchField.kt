package com.ryen.bondhub.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary

@Composable
fun SearchField(
    searchText: String,
    onValueChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    searchChat: Boolean
) {
    TextField(
        value = searchText,
        onValueChange = onValueChange,
        placeholder = { val p = if(searchChat) "Search chat" else "Find friends by email"
            Text(text = p, color = Secondary.copy(alpha = .5f), style = MaterialTheme.typography.bodyMedium)
        },
        leadingIcon = {
            if(searchChat){
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Primary
                    )
                }
            } else{
                Icon(
                    painter = painterResource(R.drawable.friends_find),
                    contentDescription = "Find Friend",
                    modifier = Modifier.size(22.dp),
                    tint = Secondary.copy(alpha = .8f)
                )
            }
        },
        trailingIcon = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = Primary
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchClick() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = Primary,
                shape = RoundedCornerShape(50)
            ),
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchFieldPrev() {
    SearchField(
        searchText = "",
        onValueChange = {},
        onBackClick = {},
        onSearchClick = {},
        searchChat = false
    )
}
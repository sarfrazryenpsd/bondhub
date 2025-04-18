package com.ryen.bondhub.presentation.contents

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.ryen.bondhub.R
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.model.MessageType
import com.ryen.bondhub.presentation.theme.MessageUser
import com.ryen.bondhub.presentation.theme.Primary
import com.ryen.bondhub.presentation.theme.Secondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageScreenContent(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    canSendMessage: Boolean,
    onSendMessage: (String) -> Unit,
    onAttachImage: (Uri) -> Unit,
    paddingValues: PaddingValues
) {
    val messageInputText = remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    var showEmojiPicker by remember { mutableStateOf(false) }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Messages list
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageItem(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUserId
                )
            }
        }

        // Message input
        Column {
            MessageInputBar(
                text = messageInputText.value,
                onTextChange = { messageInputText.value = it },
                onSendClick = {
                    if (messageInputText.value.isNotBlank() && canSendMessage) {
                        onSendMessage(messageInputText.value)
                        messageInputText.value = ""
                    }
                },
                onEmojiClick = {showEmojiPicker = !showEmojiPicker},
                showEmojiPicker = showEmojiPicker,
                onAttachClick = {
                    // Image picker would be implemented here
                    // For now just show a toast
                    Toast.makeText(context, "Attachment feature coming soon", Toast.LENGTH_SHORT)
                        .show()
                },
                canSend = canSendMessage && messageInputText.value.isNotBlank()
            )
        }
    }
}

// Individual message item
@Composable
fun MessageItem(
    message: ChatMessage,
    isFromCurrentUser: Boolean
) {
    val bubbleColor = if (isFromCurrentUser) {
        MessageUser
    } else {
        Secondary
    }

    val textColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    val dateFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeString = remember(message.timestamp) {
        dateFormatter.format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                        bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                when (message.messageType) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.content,
                            color = textColor,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
                        )
                    }
                    MessageType.IMAGE -> {
                        // Image message rendering will be implemented later
                        Text(
                            text = "[Image]",
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        Text(
                            text = "[Unsupported message type]",
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.7f)
                    )

                    // Show message status icon for current user's messages
                    if (isFromCurrentUser) {
                        when (message.status) {
                            MessageStatus.SENDING -> Icon(
                                painterResource(R.drawable.loading),
                                contentDescription = "Sending",
                                modifier = Modifier.size(12.dp),
                                tint = textColor.copy(alpha = 0.7f)
                            )
                            MessageStatus.SENT -> Icon(
                                painterResource(R.drawable.double_tick),
                                contentDescription = "Sent",
                                modifier = Modifier.size(12.dp),
                                tint = textColor.copy(alpha = 0.7f)
                            )
                            MessageStatus.DELIVERED -> Icon(
                                Icons.Default.Done,
                                contentDescription = "Delivered",
                                modifier = Modifier.size(12.dp),
                                tint = textColor.copy(alpha = 0.7f)
                            )
                            MessageStatus.READ -> Icon(
                                painterResource(R.drawable.double_tick),
                                contentDescription = "Read",
                                modifier = Modifier.size(12.dp),
                                tint = Color.Blue.copy(alpha = 0.9f)
                            )
                            MessageStatus.FAILED -> Icon(
                                Icons.Default.Warning,
                                contentDescription = "Failed",
                                modifier = Modifier.size(12.dp),
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}

// Input bar for typing and sending messages
@Composable
fun MessageInputBar(
    text: String,
    showEmojiPicker: Boolean,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onEmojiClick: () -> Unit = {},
    onAttachClick: () -> Unit,
    canSend: Boolean
) {
    Column {
        AnimatedContent(
            targetState = showEmojiPicker,
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
            if(targetState){
                EmojiPicker(
                    onEmojiSelected = { emoji ->
                        onTextChange(text + emoji)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            color = com.ryen.bondhub.presentation.theme.Surface,
            shape = CircleShape
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {

                // Message TextField with emoji prefix
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    placeholder = { Text("Type a message") },
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .15f),
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .2f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = {
                        IconButton(onClick = onEmojiClick) {
                            Icon(
                                painter = painterResource(R.drawable.emoji),
                                contentDescription = "Emoji",
                                tint = if(showEmojiPicker) Color.Red.copy(alpha = 0.5f) else Secondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Plus Icon Outline Circle Button
                OutlinedButton(
                    onClick = onAttachClick,
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Send Icon Filled Circle Button
                Button(
                    onClick = onSendClick,
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp),
                    enabled = canSend,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSend) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.send),
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmojiPicker(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf( "😁", "😂", "❤️", "🔥", "👍", "🎉")

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .height(60.dp)
            .clip(CircleShape)
            .background(Color.Red.copy(alpha = .1f))
            .padding(8.dp)
    ) {
        items(emojis) { emoji ->
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier
                    .clickable { onEmojiSelected(emoji) }
                    .padding(8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun EmojiPickerPrev() {
    EmojiPicker(onEmojiSelected = {})
}


@Preview
@Composable
private fun MessageItemPrev1() {
    MessageItem(
        message = ChatMessage(
            chatId = "123",
            senderId = "456",
            receiverId = "789",
            content = "Hello, world! This is a preview message and I'm not responsible for anything. Bye!",
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.TEXT,
            status = MessageStatus.SENT
        ),
        isFromCurrentUser = true
    )
}

@Preview
@Composable
private fun MessageItemPrev2() {
    MessageItem(
        message = ChatMessage(
            chatId = "123",
            senderId = "456",
            receiverId = "789",
            content = "Hello, world! This is a preview message and I'm not responsible for anything. Bye!",
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.TEXT,
            status = MessageStatus.SENT
        ),
        isFromCurrentUser = false
    )
}

@Preview
@Composable
private fun MessageInputBarPrev() {
    MessageInputBar(
        text = "",
        onTextChange = {},
        onSendClick = {},
        onAttachClick = {},
        canSend = false,
        showEmojiPicker = true
    )
}

/*
@Preview
@Composable
private fun MessageScreenContentPrev() {
    ChatMessageScreenContent(
        messages = listOf(
            ChatMessage(
                chatId = "123",
                senderId = "456",
                receiverId = "789",
                content = "Hello, world! This is a preview message and I'm not responsible for anything. Bye!",
                timestamp = System.currentTimeMillis(),
                messageType = MessageType.TEXT,
                status = MessageStatus.SENT
            ),
            ChatMessage(
                chatId = "123",
                senderId = "456",
                receiverId = "789",
                content = "Hello, world! This is a preview message and I'm not responsible for anything. Bye!",
                timestamp = System.currentTimeMillis(),
                messageType = MessageType.TEXT,
                status = MessageStatus.SENT
            ),
        ),
        isLoading = false,
        canSendMessage = true,
        onSendMessage = {},
        onAttachImage = {},
        paddingValues = PaddingValues(0.dp)
    )
}*/

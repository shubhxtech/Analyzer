import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.analyzer.presentation.ChatMessage
import com.example.analyzer.presentation.ChatViewModel
import com.example.analyzer.presentation.ChatViewModelFactory
import com.example.analyzer.presentation.NetworkModule

// Define your colors
val AccentBlue = Color(0xFF2196F3)
val Gray900 = Color(0xFF212121)
val Gray700 = Color(0xFF616161)
val White = Color.White

@Composable
fun ChatBotFloatingButton(modifier: Modifier = Modifier) {
    var showChatDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = { showChatDialog = true },
            shape = CircleShape,
            containerColor = AccentBlue,
            contentColor = White,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Chat,
                contentDescription = "Chat Bot",
                modifier = Modifier.size(24.dp)
            )
        }

        if (showChatDialog) {
            ChatBotDialog(
                onDismiss = { showChatDialog = false }
            )
        }
    }
}

@Composable
fun ChatBotDialog(
    onDismiss: () -> Unit
) {
    val factory = ChatViewModelFactory(NetworkModule.chatRepository)
    val chatViewModel: ChatViewModel = viewModel(factory = factory)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            ChatScreen(
                onClose = onDismiss,
                chatViewModel = chatViewModel
            )
        }
    }
}

@Composable
fun ChatScreen(
    onClose: () -> Unit,
    chatViewModel: ChatViewModel
) {
    val chatMessages by chatViewModel.chatMessages.observeAsState(initial = emptyList())
    val isLoading by chatViewModel.isLoading.observeAsState(initial = true)
    val errorMessage by chatViewModel.errorMessage.observeAsState(initial = null)
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI Assistant",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row {
                // Clear chat button
                if (chatMessages.isNotEmpty()) {
                    IconButton(onClick = { chatViewModel.clearChat() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Close button
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Chat",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Error message
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            errorMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = it,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { chatViewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }
        }

        // Chat messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { message ->
                MessageBubble(message)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Gray900
                            ),
                            modifier = Modifier.padding(end = 64.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = White,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = { Text("Ask me anything...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Gray900,
                    unfocusedContainerColor = Gray900,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = AccentBlue,
                    focusedIndicatorColor = AccentBlue,
                    unfocusedIndicatorColor = Gray700
                ),
                maxLines = 3,
                enabled = !isLoading
            )

            IconButton(
                onClick = {
                    if (userInput.isNotBlank() && !isLoading) {
                        chatViewModel.sendMessage(userInput)
                        userInput = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AccentBlue)
                    .size(48.dp),
                enabled = userInput.isNotBlank() && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message",
                    tint = White
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) AccentBlue else Gray900
            ),
            modifier = Modifier.padding(
                end = if (message.isFromUser) 0.dp else 64.dp,
                start = if (message.isFromUser) 64.dp else 0.dp
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(16.dp),
                color = White
            )
        }
    }
}
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun prevChatbot(){
    ChatBotDialog({})
}
package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.BusinessAvailability
import com.example.data.model.BusinessSettings
import com.example.data.model.ChatMessage
import com.example.ui.components.BusinessStatusPill
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EncodedOrNetworkImage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.GoldenYellowSoft
import com.example.util.ImageUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessagesScreen(
    messages: List<ChatMessage>,
    businessSettings: BusinessSettings,
    onMarkRead: () -> Unit,
    onSendMessage: (text: String, imageDataUri: String, onSent: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var pendingImage by remember { mutableStateOf("") }

    val availability = businessSettings.availabilityEnum()

    LaunchedEffect(messages.size) {
        onMarkRead()
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val encoded = ImageUtils.encodeUriToBoundedDataUri(context, uri)
                    if (encoded != null) pendingImage = encoded
                }
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with live Business Availability Status
        Surface(color = DeepNavy, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = strings.messagesTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = businessSettings.businessName,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldenYellow
                    )
                }
                BusinessStatusPill(status = availability)
            }
        }

        // Busy / Offline friendly notice (messaging always stays open!)
        if (availability == BusinessAvailability.BUSY || availability == BusinessAvailability.OFFLINE) {
            Surface(
                color = GoldenYellowSoft,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = strings.statusBusyOrOfflineBanner,
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepNavy
                    )
                }
            }
        }

        // Messages list
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.padding(16.dp).align(Alignment.Center)) {
                    EmptyStateCard(
                        icon = Icons.Rounded.ChatBubbleOutline,
                        message = strings.chatEmptyHint
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isCustomer = msg.senderRole == "customer"
                        ChatBubble(message = msg, isMine = isCustomer)
                    }
                }
            }
        }

        // Pending image preview
        if (pendingImage.isNotBlank()) {
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(8.dp)
                ) {
                    EncodedOrNetworkImage(
                        imageSource = pendingImage,
                        contentDescription = "Selected photo",
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp))
                    )
                    Text(
                        text = "1 photo attached",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepNavy
                    )
                    IconButton(onClick = { pendingImage = "" }) {
                        Text("×", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }

        // Composer (always enabled regardless of business Online/Busy/Offline status)
        Surface(color = Color.White, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.testTag("chat_attach_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = strings.attachPhotoDesc,
                        tint = ElectricBlue
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(strings.typeMessageHint) },
                    maxLines = 4,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )

                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        val txt = inputText
                        val img = pendingImage
                        if (txt.isNotBlank() || img.isNotBlank()) {
                            onSendMessage(txt, img) {
                                inputText = ""
                                pendingImage = ""
                            }
                        }
                    },
                    enabled = inputText.isNotBlank() || pendingImage.isNotBlank(),
                    modifier = Modifier.testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = strings.sendBtn,
                        tint = if (inputText.isNotBlank() || pendingImage.isNotBlank()) ElectricBlue else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isMine: Boolean) {
    val strings = LocalAppStrings.current
    val timeText = remember(message.sentAt) {
        val date = message.sentAt?.toDate() ?: Date()
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
    }

    Row(
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMine) DeepNavy else Color.White
            ),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMine) 18.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 18.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.imageUrl.isNotBlank()) {
                    EncodedOrNetworkImage(
                        imageSource = message.imageUrl,
                        contentDescription = "Chat attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    if (message.text.isNotBlank()) Spacer(Modifier.height(8.dp))
                }
                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isMine) Color.White else DeepNavy
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isMine) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.DoneAll,
                            contentDescription = if (message.readAt != null) strings.readLabel else strings.sentLabel,
                            tint = if (message.readAt != null) GoldenYellow else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

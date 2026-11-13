package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.BusinessAvailability
import com.example.data.model.BusinessSettings
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceItem
import com.example.data.model.ServiceReview
import com.example.data.model.UserProfile
import com.example.ui.components.BookingStatusBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EncodedOrNetworkImage
import com.example.ui.components.StarRatingRow
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueSoft
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.GoldenYellowSoft
import com.example.util.ImageUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminConsoleScreen(
    bookings: List<Booking>,
    conversations: List<Conversation>,
    selectedConversationId: String?,
    activeMessages: List<ChatMessage>,
    categories: List<ServiceCategory>,
    services: List<ServiceItem>,
    customers: List<UserProfile>,
    reviews: List<ServiceReview>,
    businessSettings: BusinessSettings,
    isRootMode: Boolean = false,
    snackbarHostState: androidx.compose.material3.SnackbarHostState? = null,
    onSwitchToCustomerView: (() -> Unit)? = null,
    onSignOut: (() -> Unit)? = null,
    onBack: () -> Unit,
    onSeedCatalog: () -> Unit,
    onUpdateBooking: (Booking, BookingStatus, String) -> Unit,
    onSelectConversation: (String?) -> Unit,
    onMarkConversationRead: (String) -> Unit = {},
    onSendReply: (customerId: String, text: String, imageDataUri: String, onSent: () -> Unit) -> Unit,
    onSaveCategory: (String?, String, String, String, String, String, String, Long) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onSaveService: (String?, String, String, String, String, String, List<String>, List<String>, Double, String, String, String, Boolean) -> Unit,
    onDeleteService: (String) -> Unit,
    onUpdateBusinessSettings: (String, String, String, String, String, String, String, String, String, BusinessAvailability, String, String) -> Unit,
    onModerateReview: (String, String) -> Unit,
    onSendAnnouncement: (String, String, String, String) -> Unit,
    onUpdateCustomerStatus: (String, String) -> Unit
) {
    val strings = LocalAppStrings.current
    BackHandler(enabled = !isRootMode, onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    val pendingBookingsCount = bookings.count { it.statusEnum() == BookingStatus.PENDING }
    val totalUnreadMessages = conversations.sumOf { it.unreadByAdmin }.toInt()
    val tabs = listOf("Reports", "Bookings", "Chats", "Catalog", "Business", "More")

    Scaffold(
        snackbarHost = {
            if (snackbarHostState != null) {
                androidx.compose.material3.SnackbarHost(snackbarHostState)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(strings.adminConsoleTitle) },
                navigationIcon = {
                    if (!isRootMode) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (onSwitchToCustomerView != null) {
                        IconButton(onClick = onSwitchToCustomerView) {
                            Icon(Icons.Rounded.Storefront, contentDescription = "Customer View")
                        }
                    }
                    if (onSignOut != null) {
                        IconButton(onClick = onSignOut) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = strings.logoutBtn)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = GoldenYellow
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    val badgeCount = when (index) {
                        1 -> pendingBookingsCount
                        2 -> totalUnreadMessages
                        else -> 0
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(title)
                                if (badgeCount > 0) {
                                    Badge(
                                        containerColor = if (index == 2) Color(0xFFDC2626) else GoldenYellow,
                                        contentColor = if (index == 2) Color.White else DeepNavy
                                    ) {
                                        Text(badgeCount.toString())
                                    }
                                }
                            }
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> AdminReportsTab(
                    customers = customers,
                    bookings = bookings,
                    conversations = conversations,
                    services = services,
                    onSeedCatalog = onSeedCatalog,
                    onNavigateToTab = { selectedTab = it },
                    onOpenChat = { convId ->
                        onSelectConversation(convId)
                        selectedTab = 2
                    }
                )
                1 -> AdminBookingsTab(
                    bookings = bookings,
                    conversations = conversations,
                    onUpdateBooking = onUpdateBooking,
                    onOpenCustomerChat = { customerId ->
                        onSelectConversation(customerId)
                        selectedTab = 2
                    }
                )
                2 -> AdminChatsTab(
                    conversations = conversations,
                    selectedId = selectedConversationId,
                    messages = activeMessages,
                    onSelect = onSelectConversation,
                    onMarkRead = onMarkConversationRead,
                    onSendReply = onSendReply
                )
                3 -> AdminCatalogTab(
                    categories = categories,
                    services = services,
                    onSeedCatalog = onSeedCatalog,
                    onSaveCategory = onSaveCategory,
                    onDeleteCategory = onDeleteCategory,
                    onSaveService = onSaveService,
                    onDeleteService = onDeleteService
                )
                4 -> AdminBusinessTab(businessSettings, onUpdateBusinessSettings)
                5 -> AdminMoreTab(
                    reviews = reviews,
                    customers = customers,
                    onModerateReview = onModerateReview,
                    onSendAnnouncement = onSendAnnouncement,
                    onUpdateCustomerStatus = onUpdateCustomerStatus
                )
            }
        }
    }
}

@Composable
private fun AdminReportsTab(
    customers: List<UserProfile>,
    bookings: List<Booking>,
    conversations: List<Conversation>,
    services: List<ServiceItem>,
    onSeedCatalog: () -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onOpenChat: (String) -> Unit
) {
    val pending = bookings.count { it.statusEnum() == BookingStatus.PENDING }
    val completed = bookings.count { it.statusEnum() == BookingStatus.COMPLETED }
    val cancelled = bookings.count { it.statusEnum() == BookingStatus.CANCELLED }
    val totalUnreadMessages = conversations.sumOf { it.unreadByAdmin }
    val attentionChats = conversations.sortedWith(
        compareByDescending<Conversation> { it.unreadByAdmin > 0L }
            .thenByDescending { it.lastMessageAt?.seconds ?: 0L }
    ).take(5)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                label = "Unread Messages",
                value = totalUnreadMessages.toString(),
                highlight = totalUnreadMessages > 0L,
                onClick = { onNavigateToTab(2) },
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Pending Bookings",
                value = pending.toString(),
                highlight = pending > 0,
                onClick = { onNavigateToTab(1) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Total Customers", customers.size.toString(), modifier = Modifier.weight(1f))
            StatCard("Total Bookings", bookings.size.toString(), onClick = { onNavigateToTab(1) }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Completed Jobs", completed.toString(), modifier = Modifier.weight(1f))
            StatCard("Cancelled", cancelled.toString(), modifier = Modifier.weight(1f))
        }
        StatCard(
            label = "Active Services",
            value = services.count { it.availability == "available" }.toString(),
            onClick = { onNavigateToTab(3) },
            modifier = Modifier.fillMaxWidth()
        )

        if (attentionChats.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Live Customer Message Monitor",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepNavy
                )
                TextButton(onClick = { onNavigateToTab(2) }) {
                    Text("Open All Chats")
                }
            }
            attentionChats.forEach { conv ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (conv.unreadByAdmin > 0L) GoldenYellowSoft else Color.White
                    ),
                    border = if (conv.unreadByAdmin > 0L) BorderStroke(1.dp, GoldenYellow) else null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChat(conv.id) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = conv.customerName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (conv.unreadByAdmin > 0L) FontWeight.Bold else FontWeight.SemiBold,
                                color = DeepNavy
                            )
                            Text(
                                text = conv.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        if (conv.unreadByAdmin > 0L) {
                            Surface(color = Color(0xFFDC2626), shape = RoundedCornerShape(50)) {
                                Text(
                                    text = "${conv.unreadByAdmin} new",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            TextButton(onClick = { onOpenChat(conv.id) }) {
                                Text("Reply")
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onSeedCatalog,
            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ensure Default 13 Categories & Services Seeded")
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) GoldenYellowSoft else Color.White
        ),
        border = if (highlight) BorderStroke(1.dp, GoldenYellow) else null,
        shape = RoundedCornerShape(16.dp),
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = DeepNavy, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminBookingsTab(
    bookings: List<Booking>,
    conversations: List<Conversation>,
    onUpdateBooking: (Booking, BookingStatus, String) -> Unit,
    onOpenCustomerChat: (String) -> Unit
) {
    var editing by remember { mutableStateOf<Booking?>(null) }
    var notes by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }

    if (editing != null) {
        val target = editing!!
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Update ${target.bookingCode}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Admin Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BookingStatus.entries.forEach { st ->
                            FilterChip(
                                selected = target.statusEnum() == st,
                                onClick = {
                                    onUpdateBooking(target, st, notes)
                                    editing = null
                                },
                                label = { Text(st.wireValue) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { editing = null }) { Text("Close") }
            }
        )
    }

    val filteredBookings = remember(bookings, statusFilter) {
        when (statusFilter) {
            "pending" -> bookings.filter { it.statusEnum() == BookingStatus.PENDING }
            "active" -> bookings.filter {
                it.statusEnum() in setOf(BookingStatus.ACCEPTED, BookingStatus.SCHEDULED, BookingStatus.IN_PROGRESS)
            }
            "completed" -> bookings.filter { it.statusEnum() == BookingStatus.COMPLETED }
            else -> bookings
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            listOf("all" to "All (${bookings.size})", "pending" to "Pending", "active" to "Active", "completed" to "Completed")
                .forEach { (key, label) ->
                    FilterChip(
                        selected = statusFilter == key,
                        onClick = { statusFilter = key },
                        label = { Text(label) }
                    )
                }
        }

        if (filteredBookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                EmptyStateCard(
                    icon = Icons.AutoMirrored.Rounded.Chat,
                    message = "No bookings matching the selected filter yet."
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredBookings, key = { it.id }) { b ->
                    val hasChat = conversations.any { it.customerId == b.customerId }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(14.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(b.bookingCode, color = ElectricBlue, fontWeight = FontWeight.Bold)
                                BookingStatusBadge(b.statusEnum())
                            }
                            Text("${b.customerName} • ${b.phone}", style = MaterialTheme.typography.titleSmall)
                            Text(b.serviceNameEn, style = MaterialTheme.typography.bodyMedium)
                            Text("${b.address}, ${b.cityArea}", style = MaterialTheme.typography.bodySmall)
                            Text("${b.preferredDate} • ${b.preferredTime}", style = MaterialTheme.typography.bodySmall)
                            if (b.description.isNotBlank()) {
                                Text(
                                    text = b.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (b.imageUrls.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    b.imageUrls.take(3).forEach { uri ->
                                        EncodedOrNetworkImage(
                                            imageSource = uri,
                                            contentDescription = "Customer problem photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { editing = b; notes = b.adminNotes },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                                ) {
                                    Text("Update Status")
                                }
                                if (hasChat) {
                                    OutlinedButton(onClick = { onOpenCustomerChat(b.customerId) }) {
                                        Text("Open Chat")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminChatsTab(
    conversations: List<Conversation>,
    selectedId: String?,
    messages: List<ChatMessage>,
    onSelect: (String?) -> Unit,
    onMarkRead: (String) -> Unit,
    onSendReply: (customerId: String, text: String, imageDataUri: String, onSent: () -> Unit) -> Unit
) {
    if (selectedId == null) {
        if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                EmptyStateCard(
                    icon = Icons.AutoMirrored.Rounded.Chat,
                    message = "No customer messages yet. Incoming customer chats will appear here in real time."
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversations, key = { it.id }) { conv ->
                    val timeLabel = remember(conv.lastMessageAt) {
                        conv.lastMessageAt?.toDate()?.let {
                            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(it)
                        } ?: ""
                    }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (conv.unreadByAdmin > 0) GoldenYellowSoft else Color.White
                        ),
                        border = if (conv.unreadByAdmin > 0) BorderStroke(1.dp, GoldenYellow) else null,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(conv.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = conv.customerName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (conv.unreadByAdmin > 0) FontWeight.Bold else FontWeight.SemiBold,
                                        color = DeepNavy
                                    )
                                    if (conv.customerUsername.isNotBlank()) {
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "@${conv.customerUsername}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ElectricBlue
                                        )
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = conv.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = DeepNavy.copy(alpha = 0.85f)
                                )
                                if (timeLabel.isNotBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = timeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (conv.unreadByAdmin > 0) {
                                Spacer(Modifier.width(8.dp))
                                Surface(color = Color(0xFFDC2626), shape = RoundedCornerShape(50)) {
                                    Text(
                                        text = "${conv.unreadByAdmin} new",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        val activeConv = remember(conversations, selectedId) {
            conversations.firstOrNull { it.id == selectedId || it.customerId == selectedId }
        }
        var reply by remember(selectedId) { mutableStateOf("") }
        var pendingImage by remember(selectedId) { mutableStateOf("") }

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

        LaunchedEffect(selectedId, messages.size) {
            onMarkRead(selectedId)
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    IconButton(onClick = { onSelect(null) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Conversations",
                            tint = DeepNavy
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeConv?.customerName ?: "Customer",
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepNavy
                        )
                        val sub = activeConv?.customerUsername?.takeIf { it.isNotBlank() }?.let { "@$it" } ?: selectedId
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg, isMine = msg.senderRole == "admin")
                }
            }

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
                            contentDescription = "Selected reply photo",
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
                        )
                        Text("Photo attached", style = MaterialTheme.typography.bodySmall, color = DeepNavy)
                        IconButton(onClick = { pendingImage = "" }) {
                            Text("×", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            Surface(color = Color.White, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.testTag("admin_chat_attach_photo")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = "Attach photo",
                            tint = ElectricBlue
                        )
                    }
                    OutlinedTextField(
                        value = reply,
                        onValueChange = { reply = it },
                        placeholder = { Text("Reply as Yusuf Electronics...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_chat_reply_input")
                    )
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            val txt = reply
                            val img = pendingImage
                            if (txt.isNotBlank() || img.isNotBlank()) {
                                onSendReply(selectedId, txt, img) {
                                    reply = ""
                                    pendingImage = ""
                                }
                            }
                        },
                        enabled = reply.isNotBlank() || pendingImage.isNotBlank(),
                        modifier = Modifier.testTag("admin_chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send Reply",
                            tint = if (reply.isNotBlank() || pendingImage.isNotBlank()) ElectricBlue else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCatalogTab(
    categories: List<ServiceCategory>,
    services: List<ServiceItem>,
    onSeedCatalog: () -> Unit,
    onSaveCategory: (String?, String, String, String, String, String, String, Long) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onSaveService: (String?, String, String, String, String, String, List<String>, List<String>, Double, String, String, String, Boolean) -> Unit,
    onDeleteService: (String) -> Unit
) {
    var editingCat by remember { mutableStateOf<ServiceCategory?>(null) }
    var showNewCatDialog by remember { mutableStateOf(false) }
    var editingSvc by remember { mutableStateOf<ServiceItem?>(null) }
    var showNewSvcDialog by remember { mutableStateOf(false) }

    if (showNewCatDialog || editingCat != null) {
        val target = editingCat
        var nameEn by remember(target) { mutableStateOf(target?.nameEn ?: "") }
        var nameBn by remember(target) { mutableStateOf(target?.nameBn ?: "") }
        var descEn by remember(target) { mutableStateOf(target?.descriptionEn ?: "") }
        var descBn by remember(target) { mutableStateOf(target?.descriptionBn ?: "") }
        AlertDialog(
            onDismissRequest = { showNewCatDialog = false; editingCat = null },
            title = { Text(if (target == null) "Add Service Category" else "Edit Service Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(nameEn, { nameEn = it }, label = { Text("Name (English)") })
                    OutlinedTextField(nameBn, { nameBn = it }, label = { Text("Name (Bangla)") })
                    OutlinedTextField(descEn, { descEn = it }, label = { Text("Description (EN)") })
                    OutlinedTextField(descBn, { descBn = it }, label = { Text("Description (BN)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameEn.isNotBlank()) {
                            onSaveCategory(
                                target?.id,
                                nameEn,
                                nameBn.ifBlank { nameEn },
                                descEn.ifBlank { nameEn },
                                descBn.ifBlank { nameBn.ifBlank { nameEn } },
                                target?.iconKey ?: "electrical",
                                target?.status ?: "active",
                                target?.sortOrder ?: (categories.size + 1).toLong()
                            )
                            showNewCatDialog = false
                            editingCat = null
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNewCatDialog = false; editingCat = null }) { Text("Cancel") }
            }
        )
    }

    if ((showNewSvcDialog || editingSvc != null) && categories.isNotEmpty()) {
        val target = editingSvc
        var selectedCatId by remember(target) { mutableStateOf(target?.categoryId ?: categories.first().id) }
        var nameEn by remember(target) { mutableStateOf(target?.nameEn ?: "") }
        var nameBn by remember(target) { mutableStateOf(target?.nameBn ?: "") }
        var descEn by remember(target) { mutableStateOf(target?.descriptionEn ?: "") }
        var descBn by remember(target) { mutableStateOf(target?.descriptionBn ?: "") }
        var priceText by remember(target) { mutableStateOf(target?.priceDisplayEn ?: "") }
        AlertDialog(
            onDismissRequest = { showNewSvcDialog = false; editingSvc = null },
            title = { Text(if (target == null) "Add Service" else "Edit Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(nameEn, { nameEn = it }, label = { Text("Service Name (EN)") })
                    OutlinedTextField(nameBn, { nameBn = it }, label = { Text("Service Name (BN)") })
                    OutlinedTextField(descEn, { descEn = it }, label = { Text("Description (EN)") })
                    OutlinedTextField(descBn, { descBn = it }, label = { Text("Description (BN)") })
                    OutlinedTextField(priceText, { priceText = it }, label = { Text("Estimated Price Display") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameEn.isNotBlank()) {
                            onSaveService(
                                target?.id,
                                selectedCatId,
                                nameEn,
                                nameBn.ifBlank { nameEn },
                                descEn.ifBlank { nameEn },
                                descBn.ifBlank { descEn.ifBlank { nameEn } },
                                target?.featuresEn ?: emptyList(),
                                target?.featuresBn ?: emptyList(),
                                target?.price ?: 0.0,
                                priceText.ifBlank { "Inspection quote" },
                                priceText.ifBlank { "পরিদর্শন সাপেক্ষে" },
                                target?.availability ?: "available",
                                target?.isPopular ?: false
                            )
                            showNewSvcDialog = false
                            editingSvc = null
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNewSvcDialog = false; editingSvc = null }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onSeedCatalog,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sync Default 13 Categories & Services to Cloud")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showNewCatDialog = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Category")
                    }
                    Button(
                        onClick = { showNewSvcDialog = true },
                        enabled = categories.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Service")
                    }
                }
            }
        }
        items(categories, key = { it.id }) { cat ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat.nameEn, style = MaterialTheme.typography.titleSmall)
                        Text("${cat.nameBn} • ${cat.status}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { editingCat = cat }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = ElectricBlue)
                    }
                    TextButton(
                        onClick = {
                            val next = if (cat.status == "active") "inactive" else "active"
                            onSaveCategory(cat.id, cat.nameEn, cat.nameBn, cat.descriptionEn, cat.descriptionBn, cat.iconKey, next, cat.sortOrder)
                        }
                    ) { Text(if (cat.status == "active") "Disable" else "Enable") }
                    IconButton(onClick = { onDeleteCategory(cat.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
        items(services, key = { it.id }) { svc ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(svc.nameEn, style = MaterialTheme.typography.titleSmall)
                        Text(svc.availability, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { editingSvc = svc }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = ElectricBlue)
                    }
                    TextButton(
                        onClick = {
                            val next = if (svc.availability == "available") "unavailable" else "available"
                            onSaveService(
                                svc.id, svc.categoryId, svc.nameEn, svc.nameBn, svc.descriptionEn, svc.descriptionBn,
                                svc.featuresEn, svc.featuresBn, svc.price, svc.priceDisplayEn, svc.priceDisplayBn, next, svc.isPopular
                            )
                        }
                    ) { Text(if (svc.availability == "available") "Pause" else "Activate") }
                    IconButton(onClick = { onDeleteService(svc.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminBusinessTab(
    settings: BusinessSettings,
    onSave: (String, String, String, String, String, String, String, String, String, BusinessAvailability, String, String) -> Unit
) {
    var name by remember(settings) { mutableStateOf(settings.businessName) }
    var phone by remember(settings) { mutableStateOf(settings.phoneNumber) }
    var whatsapp by remember(settings) { mutableStateOf(settings.whatsappNumber) }
    var email by remember(settings) { mutableStateOf(settings.email) }
    var addressEn by remember(settings) { mutableStateOf(settings.addressEn) }
    var addressBn by remember(settings) { mutableStateOf(settings.addressBn) }
    var mapsUrl by remember(settings) { mutableStateOf(settings.mapsQueryUrl) }
    var hoursEn by remember(settings) { mutableStateOf(settings.workingHoursEn) }
    var hoursBn by remember(settings) { mutableStateOf(settings.workingHoursBn) }
    var status by remember(settings) { mutableStateOf(settings.availabilityEnum()) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Business Availability Status", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BusinessAvailability.entries.forEach { av ->
                FilterChip(
                    selected = status == av,
                    onClick = { status = av },
                    label = { Text(av.wireValue.uppercase()) }
                )
            }
        }
        OutlinedTextField(name, { name = it }, label = { Text("Business Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(whatsapp, { whatsapp = it }, label = { Text("WhatsApp Number") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text("Business Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(addressEn, { addressEn = it }, label = { Text("Address (EN)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(addressBn, { addressBn = it }, label = { Text("Address (BN)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(mapsUrl, { mapsUrl = it }, label = { Text("Google Maps URL") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(hoursEn, { hoursEn = it }, label = { Text("Working Hours (EN)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(hoursBn, { hoursBn = it }, label = { Text("Working Hours (BN)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                onSave(
                    name, phone, whatsapp, email, addressEn, addressBn, mapsUrl,
                    hoursEn, hoursBn, status, settings.serviceAreasEn, settings.serviceAreasBn
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save Business Settings")
        }
    }
}

@Composable
private fun AdminMoreTab(
    reviews: List<ServiceReview>,
    customers: List<UserProfile>,
    onModerateReview: (String, String) -> Unit,
    onSendAnnouncement: (String, String, String, String) -> Unit,
    onUpdateCustomerStatus: (String, String) -> Unit
) {
    var titleEn by remember { mutableStateOf("") }
    var bodyEn by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(14.dp)) {
                    Text("Send Global Announcement", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(titleEn, { titleEn = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(bodyEn, { bodyEn = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (titleEn.isNotBlank() && bodyEn.isNotBlank()) {
                                onSendAnnouncement(titleEn, titleEn, bodyEn, bodyEn)
                                titleEn = ""
                                bodyEn = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) { Text("Send Announcement") }
                }
            }
        }

        item {
            Text("Customers (${customers.size})", style = MaterialTheme.typography.titleMedium)
        }
        items(customers, key = { it.id }) { cust ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${cust.fullName} (@${cust.username})", style = MaterialTheme.typography.titleSmall)
                        Text("${cust.email} • ${cust.accountStatus}", style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = {
                            val next = if (cust.accountStatus == "active") "suspended" else "active"
                            onUpdateCustomerStatus(cust.id, next)
                        }
                    ) { Text(if (cust.accountStatus == "active") "Suspend" else "Activate") }
                }
            }
        }

        item {
            Text("Reviews Moderation (${reviews.size})", style = MaterialTheme.typography.titleMedium)
        }
        items(reviews, key = { it.id }) { rev ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(rev.customerName, style = MaterialTheme.typography.titleSmall)
                        StarRatingRow(rev.rating.toInt())
                    }
                    Text(rev.comment, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onModerateReview(rev.id, "approved") }) { Text("Approve") }
                        OutlinedButton(onClick = { onModerateReview(rev.id, "hidden") }) { Text("Hide") }
                    }
                }
            }
        }
    }
}

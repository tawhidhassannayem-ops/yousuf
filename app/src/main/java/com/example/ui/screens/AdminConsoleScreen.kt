package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.components.StarRatingRow
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldenYellow

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
    onBack: () -> Unit,
    onSeedCatalog: () -> Unit,
    onUpdateBooking: (Booking, BookingStatus, String) -> Unit,
    onSelectConversation: (String?) -> Unit,
    onSendReply: (customerId: String, text: String) -> Unit,
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
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Reports", "Bookings", "Chats", "Catalog", "Business", "More")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.adminConsoleTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> AdminReportsTab(customers, bookings, conversations, services, onSeedCatalog)
                1 -> AdminBookingsTab(bookings, onUpdateBooking)
                2 -> AdminChatsTab(
                    conversations = conversations,
                    selectedId = selectedConversationId,
                    messages = activeMessages,
                    onSelect = onSelectConversation,
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
    onSeedCatalog: () -> Unit
) {
    val pending = bookings.count { it.statusEnum() == BookingStatus.PENDING }
    val completed = bookings.count { it.statusEnum() == BookingStatus.COMPLETED }
    val cancelled = bookings.count { it.statusEnum() == BookingStatus.CANCELLED }
    val unreadChats = conversations.count { it.unreadByAdmin > 0 }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Total Customers", customers.size.toString(), Modifier.weight(1f))
            StatCard("Total Bookings", bookings.size.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Pending Bookings", pending.toString(), Modifier.weight(1f))
            StatCard("Completed Jobs", completed.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Cancelled", cancelled.toString(), Modifier.weight(1f))
            StatCard("Unread Chats", unreadChats.toString(), Modifier.weight(1f))
        }
        StatCard("Active Services", services.count { it.availability == "available" }.toString(), Modifier.fillMaxWidth())

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
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
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
    onUpdateBooking: (Booking, BookingStatus, String) -> Unit
) {
    var editing by remember { mutableStateOf<Booking?>(null) }
    var notes by remember { mutableStateOf("") }

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

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(bookings, key = { it.id }) { b ->
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
                    Button(
                        onClick = { editing = b; notes = b.adminNotes },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        Text("Update Status")
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
    onSendReply: (customerId: String, text: String) -> Unit
) {
    if (selectedId == null) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(conversations, key = { it.id }) { conv ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            Text(conv.customerName, style = MaterialTheme.typography.titleSmall, color = DeepNavy)
                            Text(conv.lastMessage, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                        if (conv.unreadByAdmin > 0) {
                            Surface(color = GoldenYellow, shape = RoundedCornerShape(50)) {
                                Text(
                                    text = "${conv.unreadByAdmin} new",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeepNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        var reply by remember { mutableStateOf("") }
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                    TextButton(onClick = { onSelect(null) }) { Text("< Conversations") }
                    Text("Customer: $selectedId", style = MaterialTheme.typography.labelMedium)
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg, isMine = msg.senderRole == "admin")
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(8.dp)
            ) {
                OutlinedTextField(
                    value = reply,
                    onValueChange = { reply = it },
                    placeholder = { Text("Reply as Yusuf Electronics...") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (reply.isNotBlank()) {
                            onSendReply(selectedId, reply)
                            reply = ""
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send Reply", tint = ElectricBlue)
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
    var showNewCatDialog by remember { mutableStateOf(false) }
    var showNewSvcDialog by remember { mutableStateOf(false) }

    if (showNewCatDialog) {
        var nameEn by remember { mutableStateOf("") }
        var nameBn by remember { mutableStateOf("") }
        var descEn by remember { mutableStateOf("") }
        var descBn by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewCatDialog = false },
            title = { Text("Add Service Category") },
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
                                null, nameEn, nameBn.ifBlank { nameEn },
                                descEn.ifBlank { nameEn }, descBn.ifBlank { nameBn.ifBlank { nameEn } },
                                "electrical", "active", (categories.size + 1).toLong()
                            )
                            showNewCatDialog = false
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showNewCatDialog = false }) { Text("Cancel") } }
        )
    }

    if (showNewSvcDialog && categories.isNotEmpty()) {
        var selectedCatId by remember { mutableStateOf(categories.first().id) }
        var nameEn by remember { mutableStateOf("") }
        var nameBn by remember { mutableStateOf("") }
        var descEn by remember { mutableStateOf("") }
        var priceText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewSvcDialog = false },
            title = { Text("Add Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(nameEn, { nameEn = it }, label = { Text("Service Name (EN)") })
                    OutlinedTextField(nameBn, { nameBn = it }, label = { Text("Service Name (BN)") })
                    OutlinedTextField(descEn, { descEn = it }, label = { Text("Description (EN)") })
                    OutlinedTextField(priceText, { priceText = it }, label = { Text("Estimated Price Display") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameEn.isNotBlank()) {
                            onSaveService(
                                null, selectedCatId, nameEn, nameBn.ifBlank { nameEn },
                                descEn.ifBlank { nameEn }, descEn.ifBlank { nameEn },
                                emptyList(), emptyList(), 0.0,
                                priceText.ifBlank { "Inspection quote" }, priceText.ifBlank { "পরিদর্শন সাপেক্ষে" },
                                "available", false
                            )
                            showNewSvcDialog = false
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showNewSvcDialog = false }) { Text("Cancel") } }
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

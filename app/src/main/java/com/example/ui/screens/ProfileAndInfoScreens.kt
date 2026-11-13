package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.AppNotification
import com.example.data.model.BusinessSettings
import com.example.data.model.UserProfile
import com.example.ui.components.BrandLogoMark
import com.example.ui.components.BusinessStatusPill
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EncodedOrNetworkImage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueSoft
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.GoldenYellowSoft
import com.example.ui.theme.StatusOfflineRed
import com.example.util.ImageUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    isAdmin: Boolean,
    notificationsEnabled: Boolean,
    isBusy: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onUpdateProfile: (fullName: String, username: String, phone: String, address: String, profileImage: String, onDone: () -> Unit) -> Unit,
    onOpenMyBookings: () -> Unit,
    onOpenMyMessages: () -> Unit,
    onOpenContactUs: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenAdminConsole: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            onToggleNotifications(granted)
        }
    )

    if (showEditDialog && userProfile != null) {
        var fullName by remember(userProfile) { mutableStateOf(userProfile.fullName) }
        var username by remember(userProfile) { mutableStateOf(userProfile.username) }
        var phone by remember(userProfile) { mutableStateOf(userProfile.phone) }
        var address by remember(userProfile) { mutableStateOf(userProfile.address) }
        var profileImg by remember(userProfile) { mutableStateOf(userProfile.profileImage) }

        val avatarPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (uri != null) {
                    scope.launch {
                        ImageUtils.encodeUriToBoundedDataUri(context, uri)?.let { profileImg = it }
                    }
                }
            }
        )

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(strings.editProfileBtn) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedButton(
                        onClick = { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    ) {
                        Text(strings.addPhotoBtn)
                    }
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text(strings.fullNameLabel) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.lowercase().filter { ch -> ch.isLetterOrDigit() || ch == '_' } },
                        label = { Text(strings.usernameLabel) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(strings.phoneOptionalLabel) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(strings.addressLabel) },
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullName.trim().isNotEmpty() && username.trim().length >= 3) {
                            onUpdateProfile(fullName, username, phone, address, profileImg) {
                                showEditDialog = false
                            }
                        }
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text(strings.confirmBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text(strings.cancelBtn) }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteAccountConfirmTitle) },
            text = { Text(strings.deleteAccountConfirmBody) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusOfflineRed)
                ) {
                    Text(strings.confirmBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(strings.cancelBtn) }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Surface(color = DeepNavy, modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Surface(
                    color = ElectricBlueSoft,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(84.dp)
                        .border(2.dp, GoldenYellow, CircleShape)
                ) {
                    if (!userProfile?.profileImage.isNullOrBlank()) {
                        EncodedOrNetworkImage(
                            imageSource = userProfile!!.profileImage,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(44.dp))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = userProfile?.fullName ?: "Customer",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                if (!userProfile?.username.isNullOrBlank()) {
                    Text(
                        text = "@${userProfile!!.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldenYellow
                    )
                }
                if (!userProfile?.email.isNullOrBlank()) {
                    Text(
                        text = userProfile!!.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                if (isAdmin) {
                    Spacer(Modifier.height(8.dp))
                    Surface(color = GoldenYellow, shape = RoundedCornerShape(50)) {
                        Text(
                            text = strings.adminBadge,
                            style = MaterialTheme.typography.labelMedium,
                            color = DeepNavy,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            if (isAdmin) {
                Button(
                    onClick = onOpenAdminConsole,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow, contentColor = DeepNavy),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("profile_admin_console_button")
                ) {
                    Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.adminConsoleTitle, fontWeight = FontWeight.Bold)
                }
            }

            // Account details card
            if (userProfile != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val createdStr = remember(userProfile.createdAt) {
                            userProfile.createdAt?.toDate()?.let {
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
                            } ?: "-"
                        }
                        ProfileMetaRow(strings.phoneOptionalLabel, userProfile.phone.ifBlank { "-" })
                        ProfileMetaRow(strings.addressLabel, userProfile.address.ifBlank { "-" })
                        ProfileMetaRow(strings.accountCreatedLabel, createdStr)
                    }
                }
            }

            // Menu Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuItem(Icons.Rounded.Edit, strings.editProfileBtn) { showEditDialog = true }
                    HorizontalDivider()
                    ProfileMenuItem(Icons.Rounded.CalendarMonth, strings.myBookingsTitle, onOpenMyBookings)
                    HorizontalDivider()
                    ProfileMenuItem(Icons.AutoMirrored.Rounded.Chat, strings.messagesTitle, onOpenMyMessages)
                    HorizontalDivider()
                    ProfileMenuItem(Icons.Rounded.Call, strings.contactUsTitle, onOpenContactUs)
                    HorizontalDivider()
                    ProfileMenuItem(
                        icon = Icons.Rounded.Language,
                        label = "${strings.languageLabel} -> ${strings.switchLangAction}",
                        onClick = onToggleLanguage
                    )
                    HorizontalDivider()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = ElectricBlue)
                            Spacer(Modifier.width(12.dp))
                            Text(strings.notificationSettingsLabel, style = MaterialTheme.typography.bodyLarge)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { wantEnable ->
                                if (wantEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onToggleNotifications(true)
                                    }
                                } else {
                                    onToggleNotifications(wantEnable)
                                }
                            }
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuItem(Icons.Rounded.PrivacyTip, strings.privacyPolicyTitle, onOpenPrivacyPolicy)
                    HorizontalDivider()
                    ProfileMenuItem(Icons.Rounded.Description, strings.termsTitle, onOpenTerms)
                }
            }

            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("profile_logout_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(strings.logoutBtn)
            }

            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_delete_account_button")
            ) {
                Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = StatusOfflineRed)
                Spacer(Modifier.width(6.dp))
                Text(strings.deleteAccountBtn, color = StatusOfflineRed)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileMetaRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = DeepNavy, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ElectricBlue)
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = DeepNavy, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(
    businessSettings: BusinessSettings,
    onBack: () -> Unit,
    onSendMessage: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isBangla = strings.isBangla
    val context = LocalContext.current

    BackHandler(onBack = onBack)

    val phone = businessSettings.phoneNumber.trim()
    val whatsapp = businessSettings.whatsappNumber.trim()
    val email = businessSettings.email.trim()
    val address = if (isBangla) businessSettings.addressBn else businessSettings.addressEn
    val hours = if (isBangla) businessSettings.workingHoursBn else businessSettings.workingHoursEn
    val areas = if (isBangla) businessSettings.serviceAreasBn else businessSettings.serviceAreasEn

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.contactUsTitle) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(18.dp)
                ) {
                    BrandLogoMark(sizeDp = 56)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = businessSettings.businessName,
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldenYellow
                        )
                        Spacer(Modifier.height(4.dp))
                        BusinessStatusPill(status = businessSettings.availabilityEnum())
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
                    ContactInfoRow(Icons.Rounded.Call, strings.phoneLabel, phone.ifBlank { strings.notConfiguredYet })
                    ContactInfoRow(Icons.AutoMirrored.Rounded.Chat, strings.whatsappLabel, whatsapp.ifBlank { strings.notConfiguredYet })
                    ContactInfoRow(Icons.Rounded.Email, strings.emailLabel, email.ifBlank { strings.notConfiguredYet })
                    ContactInfoRow(Icons.Rounded.LocationOn, strings.addressContactLabel, address.ifBlank { strings.notConfiguredYet })
                    ContactInfoRow(Icons.Rounded.Schedule, strings.workingHoursLabel, hours)
                    ContactInfoRow(Icons.Rounded.LocationOn, strings.serviceAreasLabel, areas)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (phone.isNotBlank()) {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                        }
                    },
                    enabled = phone.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.callNowBtn)
                }

                Button(
                    onClick = {
                        if (whatsapp.isNotBlank()) {
                            val clean = whatsapp.filter { it.isDigit() }
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$clean")))
                        }
                    },
                    enabled = whatsapp.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text(strings.whatsappBtn)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onSendMessage,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.sendMessageBtn)
                }

                val mapUrl = businessSettings.mapsQueryUrl.trim()
                OutlinedButton(
                    onClick = {
                        val uri = if (mapUrl.isNotBlank()) {
                            Uri.parse(mapUrl)
                        } else if (address.isNotBlank()) {
                            Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                        } else null
                        if (uri != null) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    },
                    enabled = mapUrl.isNotBlank() || address.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.viewLocationBtn)
                }
            }
        }
    }
}

@Composable
private fun ContactInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(imageVector = icon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = DeepNavy)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onBack: () -> Unit,
    onMarkRead: (AppNotification) -> Unit
) {
    val strings = LocalAppStrings.current
    val isBangla = strings.isBangla
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.notificationsTitle) },
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
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                EmptyStateCard(
                    icon = Icons.Rounded.NotificationsNone,
                    message = strings.noNotificationsYet
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) Color.White else ElectricBlueSoft
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMarkRead(notif) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = notif.localizedTitle(isBangla),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DeepNavy,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!notif.isRead) {
                                    Surface(color = GoldenYellow, shape = CircleShape, modifier = Modifier.size(10.dp)) {}
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = notif.localizedBody(isBangla),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalTextScreen(
    title: String,
    body: String,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DeepNavy,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}

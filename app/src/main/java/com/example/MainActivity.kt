package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.BookingStatus
import com.example.data.model.UserProfile
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.BookingRepository
import com.example.data.repository.BusinessAndReviewsRepository
import com.example.data.repository.CatalogRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.authStateFlow
import com.example.ui.components.BrandLogoMark
import com.example.ui.components.ErrorRetryCard
import com.example.ui.localization.BanglaStrings
import com.example.ui.localization.EnglishStrings
import com.example.ui.localization.LocalAppStrings
import com.example.ui.screens.AdminConsoleScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BookingFormScreen
import com.example.ui.screens.ContactUsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LegalTextScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.MyBookingsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileRegistrationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ServiceDetailScreen
import com.example.ui.screens.ServicesCatalogScreen
import com.example.ui.screens.signOutUser
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueSoft
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.YusufElectronicsTheme
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.YusufMainViewModel
import com.example.data.seed.DefaultCatalogData
import com.example.util.LocalNotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class MainTab {
    HOME,
    SERVICES,
    MESSAGES,
    BOOKINGS,
    PROFILE
}

private sealed interface SecondaryRoute {
    data class ServiceDetail(val serviceId: String) : SecondaryRoute
    data class BookingForm(val serviceId: String) : SecondaryRoute
    data object ContactUs : SecondaryRoute
    data object Notifications : SecondaryRoute
    data object PrivacyPolicy : SecondaryRoute
    data object Terms : SecondaryRoute
    data object AdminConsole : SecondaryRoute
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    runCatching { LocalNotificationHelper.ensureChannel(context.applicationContext) }
                }
            }
            val prefsRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
            val language by prefsRepository.languageFlow.collectAsStateWithLifecycle(initialValue = AppLanguage.BANGLA)
            val strings = if (language == AppLanguage.BANGLA) BanglaStrings else EnglishStrings

            val isFirebaseReady = remember(context) {
                try {
                    FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
                } catch (_: Throwable) {
                    false
                }
            }

            YusufElectronicsTheme {
                CompositionLocalProvider(LocalAppStrings provides strings) {
                    if (!isFirebaseReady) {
                        FirebaseUnconfiguredScreen(
                            isBangla = language == AppLanguage.BANGLA,
                            onToggleLanguage = {
                                scope.launch {
                                    val next = if (language == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
                                    prefsRepository.setLanguage(next)
                                }
                            }
                        )
                    } else {
                        val authFlow = remember { Firebase.auth.authStateFlow() }
                        val initialUser = remember { runCatching { Firebase.auth.currentUser }.getOrNull() }
                        val currentUser by authFlow
                            .collectAsStateWithLifecycle(initialValue = initialUser)
                        val user = currentUser
                        if (user == null) {
                            AuthScreen(
                                isBangla = language == AppLanguage.BANGLA,
                                onToggleLanguage = {
                                    scope.launch {
                                        val next = if (language == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
                                        prefsRepository.setLanguage(next)
                                    }
                                },
                                onAuthSuccess = {}
                            )
                        } else {
                            key(user.uid) {
                                AuthenticatedRoot(
                                    user = user,
                                    prefsRepository = prefsRepository
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedRoot(
    user: FirebaseUser,
    prefsRepository: UserPreferencesRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { runCatching { CredentialManager.create(context) }.getOrNull() }

    val viewModel: YusufMainViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                val db = FirebaseFirestore.getInstance(appContext.getString(R.string.firestore_database_id))
                YusufMainViewModel(
                    currentUserId = user.uid,
                    currentUserEmail = user.email ?: "",
                    currentUserName = user.displayName ?: "",
                    isEmailVerified = user.isEmailVerified,
                    userRepository = UserRepository(db),
                    catalogRepository = CatalogRepository(db),
                    bookingRepository = BookingRepository(db),
                    chatRepository = ChatRepository(db),
                    businessRepository = BusinessAndReviewsRepository(db),
                    preferencesRepository = prefsRepository
                )
            }
        }
    )

    val strings = LocalAppStrings.current
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isAdminResolved by viewModel.isAdminResolved.collectAsStateWithLifecycle()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    val adminBookings by viewModel.adminAllBookings.collectAsStateWithLifecycle()
    val adminConversations by viewModel.adminAllConversations.collectAsStateWithLifecycle()
    val adminSelectedConvId by viewModel.selectedAdminConversationId.collectAsStateWithLifecycle()
    val adminActiveMessages by viewModel.adminActiveMessages.collectAsStateWithLifecycle()
    val adminCustomers by viewModel.adminAllCustomers.collectAsStateWithLifecycle()
    val adminReviews by viewModel.adminAllReviews.collectAsStateWithLifecycle()

    var adminDirectMode by rememberSaveable(user.uid) { mutableStateOf(true) }
    val adminSnackbarHostState = remember { SnackbarHostState() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )
    var askedNotificationPermission by rememberSaveable(user.uid) { mutableStateOf(false) }
    LaunchedEffect(notificationsEnabled, askedNotificationPermission) {
        if (
            notificationsEnabled &&
            !askedNotificationPermission &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                askedNotificationPermission = true
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(isAdmin, adminDirectMode, actionError, actionMessage) {
        if (isAdmin && adminDirectMode) {
            val msg = actionError ?: actionMessage
            if (!msg.isNullOrBlank()) {
                adminSnackbarHostState.showSnackbar(msg)
                viewModel.clearFeedback()
            }
        }
    }

    // Trigger local Android notifications for newly arrived unread items while active
    val knownNotificationIds = remember(user.uid) { mutableStateOf<Set<String>?>(null) }
    LaunchedEffect(notifications, notificationsEnabled) {
        val currentIds = notifications.map { it.id }.toSet()
        val prev = knownNotificationIds.value
        if (prev == null) {
            knownNotificationIds.value = currentIds
        } else {
            val newUnread = notifications.filter { it.id !in prev && !it.isRead }
            if (notificationsEnabled) {
                newUnread.take(3).forEach { notif ->
                    LocalNotificationHelper.showNotification(
                        context = context,
                        idSeed = notif.id,
                        title = notif.localizedTitle(strings.isBangla),
                        body = notif.localizedBody(strings.isBangla)
                    )
                }
            }
            knownNotificationIds.value = prev + currentIds
        }
    }

    // Real-time Admin device alerts when any customer sends a message or creates a pending booking
    val prevConvSnapshot = remember(user.uid) { mutableStateOf<Map<String, Pair<Long, Long>>?>(null) }
    LaunchedEffect(isAdmin) {
        if (isAdmin && prevConvSnapshot.value == null) {
            kotlinx.coroutines.delay(2500L)
            if (prevConvSnapshot.value == null) {
                prevConvSnapshot.value = emptyMap()
            }
        }
    }
    LaunchedEffect(isAdmin, adminConversations, notificationsEnabled, adminDirectMode, adminSelectedConvId) {
        if (!isAdmin) return@LaunchedEffect
        val currentMap = adminConversations.associate { conv ->
            conv.id to (conv.unreadByAdmin to (conv.lastMessageAt?.toDate()?.time ?: 0L))
        }
        val prev = prevConvSnapshot.value
        if (prev == null) {
            if (adminConversations.isNotEmpty()) {
                prevConvSnapshot.value = currentMap
            }
        } else {
            if (notificationsEnabled) {
                adminConversations.forEach { conv ->
                    val old = prev[conv.id]
                    val newTs = conv.lastMessageAt?.toDate()?.time ?: 0L
                    val hasNewUnread = conv.unreadByAdmin > 0L && (
                        old == null ||
                            conv.unreadByAdmin > old.first ||
                            newTs > old.second
                        )
                    val currentlyViewing = adminDirectMode && adminSelectedConvId == conv.id
                    if (hasNewUnread && !currentlyViewing) {
                        LocalNotificationHelper.showNotification(
                            context = context,
                            idSeed = "admin_chat_${conv.id}",
                            title = if (strings.isBangla) {
                                "নতুন গ্রাহক মেসেজ: ${conv.customerName}"
                            } else {
                                "New Message from ${conv.customerName}"
                            },
                            body = conv.lastMessage.ifBlank { "Sent an attachment" }
                        )
                    }
                }
            }
            prevConvSnapshot.value = currentMap
        }
    }

    val knownAdminBookingIds = remember(user.uid) { mutableStateOf<Set<String>?>(null) }
    LaunchedEffect(isAdmin) {
        if (isAdmin && knownAdminBookingIds.value == null) {
            kotlinx.coroutines.delay(2500L)
            if (knownAdminBookingIds.value == null) {
                knownAdminBookingIds.value = emptySet()
            }
        }
    }
    LaunchedEffect(isAdmin, adminBookings, notificationsEnabled) {
        if (!isAdmin) return@LaunchedEffect
        val currentIds = adminBookings.map { it.id }.toSet()
        val prev = knownAdminBookingIds.value
        if (prev == null) {
            if (adminBookings.isNotEmpty()) {
                knownAdminBookingIds.value = currentIds
            }
        } else {
            if (notificationsEnabled) {
                adminBookings
                    .filter { it.id !in prev && it.statusEnum() == BookingStatus.PENDING }
                    .take(3)
                    .forEach { b ->
                        LocalNotificationHelper.showNotification(
                            context = context,
                            idSeed = "admin_booking_${b.id}",
                            title = if (strings.isBangla) {
                                "নতুন বুকিং: ${b.bookingCode}"
                            } else {
                                "New Booking: ${b.bookingCode}"
                            },
                            body = "${b.customerName} • ${b.serviceNameEn}"
                        )
                    }
            }
            knownAdminBookingIds.value = prev + currentIds
        }
    }

    if (!isAdminResolved) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(DeepNavy)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BrandLogoMark(sizeDp = 96)
                Spacer(Modifier.height(18.dp))
                CircularProgressIndicator(color = GoldenYellow)
            }
        }
        return
    }

    if (isAdmin && adminDirectMode) {
        val categoriesState by viewModel.categoriesState.collectAsStateWithLifecycle()
        val servicesState by viewModel.servicesState.collectAsStateWithLifecycle()
        val businessSettings by viewModel.businessSettings.collectAsStateWithLifecycle()

        val categories = (categoriesState as? UiState.Success)?.data ?: emptyList()
        val services = (servicesState as? UiState.Success)?.data ?: emptyList()

        AdminConsoleScreen(
            bookings = adminBookings,
            conversations = adminConversations,
            selectedConversationId = adminSelectedConvId,
            activeMessages = adminActiveMessages,
            categories = categories,
            services = services,
            customers = adminCustomers,
            reviews = adminReviews,
            businessSettings = businessSettings,
            isRootMode = true,
            snackbarHostState = adminSnackbarHostState,
            onSwitchToCustomerView = { adminDirectMode = false },
            onSignOut = { signOutUser(credentialManager, scope) },
            onBack = {},
            onSeedCatalog = viewModel::adminSeedDefaultCatalog,
            onUpdateBooking = viewModel::adminUpdateBookingStatus,
            onSelectConversation = viewModel::adminSelectConversation,
            onMarkConversationRead = viewModel::markAdminConversationRead,
            onSendReply = { customerId, text, imageUri, onSent ->
                viewModel.adminSendReply(customerId, text, imageUri, onSent)
            },
            onSaveCategory = viewModel::adminSaveCategory,
            onDeleteCategory = viewModel::adminDeleteCategory,
            onSaveService = viewModel::adminSaveService,
            onDeleteService = viewModel::adminDeleteService,
            onUpdateBusinessSettings = viewModel::adminUpdateBusinessSettings,
            onModerateReview = viewModel::adminModerateReview,
            onSendAnnouncement = viewModel::adminSendAnnouncement,
            onUpdateCustomerStatus = viewModel::adminUpdateCustomerStatus
        )
        return
    }

    when (val state = profileState) {
        is UiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepNavy)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BrandLogoMark(sizeDp = 96)
                    Spacer(Modifier.height(18.dp))
                    CircularProgressIndicator(color = GoldenYellow)
                }
            }
        }

        is UiState.Error -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ErrorRetryCard(
                        message = state.message,
                        onRetry = {
                            signOutUser(credentialManager, scope)
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { signOutUser(credentialManager, scope) }) {
                        Text(strings.logoutBtn)
                    }
                }
            }
        }

        is UiState.Success -> {
            val profile = state.data
            if (profile == null && !isAdmin) {
                ProfileRegistrationScreen(
                    initialName = viewModel.defaultDisplayName,
                    email = viewModel.defaultEmail,
                    isBusy = isBusy,
                    errorMessage = actionError,
                    onToggleLanguage = viewModel::toggleLanguage,
                    onSubmit = { fullName, username, phone, address ->
                        viewModel.createProfile(fullName, username, phone, address)
                    },
                    onSignOut = {
                        signOutUser(credentialManager, scope)
                    }
                )
            } else {
                val effectiveProfile = profile ?: UserProfile(
                    userId = user.uid,
                    fullName = viewModel.defaultDisplayName.ifBlank { "Yusuf Admin" },
                    username = "yusuf_admin",
                    email = viewModel.defaultEmail,
                    role = "customer"
                )
                YusufAppShell(
                    profile = effectiveProfile,
                    viewModel = viewModel,
                    onOpenAdminRoot = { adminDirectMode = true },
                    onSignOut = {
                        signOutUser(credentialManager, scope)
                    }
                )
            }
        }
    }
}

@Composable
private fun YusufAppShell(
    profile: UserProfile,
    viewModel: YusufMainViewModel,
    onOpenAdminRoot: () -> Unit,
    onSignOut: () -> Unit
) {
    val strings = LocalAppStrings.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    val categoriesState by viewModel.categoriesState.collectAsStateWithLifecycle()
    val servicesState by viewModel.servicesState.collectAsStateWithLifecycle()
    val bookingsState by viewModel.myBookingsState.collectAsStateWithLifecycle()
    val myConversation by viewModel.myConversation.collectAsStateWithLifecycle()
    val messagesState by viewModel.myMessagesState.collectAsStateWithLifecycle()
    val businessSettings by viewModel.businessSettings.collectAsStateWithLifecycle()
    val approvedReviews by viewModel.approvedReviews.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val adminBookings by viewModel.adminAllBookings.collectAsStateWithLifecycle()
    val adminConversations by viewModel.adminAllConversations.collectAsStateWithLifecycle()
    val adminSelectedConvId by viewModel.selectedAdminConversationId.collectAsStateWithLifecycle()
    val adminActiveMessages by viewModel.adminActiveMessages.collectAsStateWithLifecycle()
    val adminCustomers by viewModel.adminAllCustomers.collectAsStateWithLifecycle()
    val adminReviews by viewModel.adminAllReviews.collectAsStateWithLifecycle()

    LaunchedEffect(actionError, actionMessage) {
        val msg = actionError ?: actionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    val categories = (categoriesState as? UiState.Success)?.data ?: emptyList()
    val services = (servicesState as? UiState.Success)?.data ?: emptyList()
    val bookings = (bookingsState as? UiState.Success)?.data ?: emptyList()
    val messages = (messagesState as? UiState.Success)?.data ?: emptyList()

    var currentTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var servicesFilterCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var homeSearchQuery by rememberSaveable { mutableStateOf("") }
    val secondaryStack = remember { mutableStateListOf<SecondaryRoute>() }

    // Back from non-home tab returns to Home first
    if (secondaryStack.isEmpty() && currentTab != MainTab.HOME) {
        BackHandler {
            currentTab = MainTab.HOME
        }
    }

    val activeSecondary = secondaryStack.lastOrNull()
    if (activeSecondary != null) {
        val popRoute: () -> Unit = {
            if (secondaryStack.isNotEmpty()) {
                secondaryStack.removeAt(secondaryStack.lastIndex)
            }
        }
        when (activeSecondary) {
            is SecondaryRoute.ServiceDetail -> {
                val service = services.firstOrNull { it.id == activeSecondary.serviceId }
                if (service == null) {
                    LaunchedEffect(Unit) { popRoute() }
                } else {
                    val category = categories.firstOrNull { it.id == service.categoryId }
                    ServiceDetailScreen(
                        service = service,
                        category = category,
                        businessSettings = businessSettings,
                        onBack = popRoute,
                        onBookThisService = {
                            secondaryStack.add(SecondaryRoute.BookingForm(service.id))
                        },
                        onSendMessage = {
                            secondaryStack.clear()
                            currentTab = MainTab.MESSAGES
                        }
                    )
                }
            }

            is SecondaryRoute.BookingForm -> {
                val service = services.firstOrNull { it.id == activeSecondary.serviceId }
                if (service == null) {
                    LaunchedEffect(Unit) { popRoute() }
                } else {
                    val category = categories.firstOrNull { it.id == service.categoryId }
                    BookingFormScreen(
                        service = service,
                        category = category,
                        userProfile = profile,
                        isBusy = isBusy,
                        errorMessage = actionError,
                        onBack = popRoute,
                        onSubmitBooking = { cName, phone, address, cityArea, desc, imageUrls, date, time, onBooked ->
                            viewModel.submitBooking(
                                service = service,
                                category = category,
                                customerName = cName,
                                phone = phone,
                                address = address,
                                cityArea = cityArea,
                                description = desc,
                                imageUrls = imageUrls,
                                preferredDate = date,
                                preferredTime = time,
                                onBooked = onBooked
                            )
                        },
                        onDoneAfterSuccess = {
                            secondaryStack.clear()
                            currentTab = MainTab.BOOKINGS
                        }
                    )
                }
            }

            SecondaryRoute.ContactUs -> {
                ContactUsScreen(
                    businessSettings = businessSettings,
                    onBack = popRoute,
                    onSendMessage = {
                        secondaryStack.clear()
                        currentTab = MainTab.MESSAGES
                    }
                )
            }

            SecondaryRoute.Notifications -> {
                NotificationsScreen(
                    notifications = notifications,
                    onBack = popRoute,
                    onMarkRead = viewModel::markNotificationRead
                )
            }

            SecondaryRoute.PrivacyPolicy -> {
                LegalTextScreen(
                    title = strings.privacyPolicyTitle,
                    body = strings.privacyPolicyBody,
                    onBack = popRoute
                )
            }

            SecondaryRoute.Terms -> {
                LegalTextScreen(
                    title = strings.termsTitle,
                    body = strings.termsBody,
                    onBack = popRoute
                )
            }

            SecondaryRoute.AdminConsole -> {
                AdminConsoleScreen(
                    bookings = adminBookings,
                    conversations = adminConversations,
                    selectedConversationId = adminSelectedConvId,
                    activeMessages = adminActiveMessages,
                    categories = categories,
                    services = services,
                    customers = adminCustomers,
                    reviews = adminReviews,
                    businessSettings = businessSettings,
                    onBack = popRoute,
                    onSeedCatalog = viewModel::adminSeedDefaultCatalog,
                    onUpdateBooking = viewModel::adminUpdateBookingStatus,
                    onSelectConversation = viewModel::adminSelectConversation,
                    onMarkConversationRead = viewModel::markAdminConversationRead,
                    onSendReply = { customerId, text, imageUri, onSent ->
                        viewModel.adminSendReply(customerId, text, imageUri, onSent)
                    },
                    onSaveCategory = viewModel::adminSaveCategory,
                    onDeleteCategory = viewModel::adminDeleteCategory,
                    onSaveService = viewModel::adminSaveService,
                    onDeleteService = viewModel::adminDeleteService,
                    onUpdateBusinessSettings = viewModel::adminUpdateBusinessSettings,
                    onModerateReview = viewModel::adminModerateReview,
                    onSendAnnouncement = viewModel::adminSendAnnouncement,
                    onUpdateCustomerStatus = viewModel::adminUpdateCustomerStatus
                )
            }
        }
        return
    }

    val unreadChats = (myConversation?.unreadByCustomer ?: 0L).toInt()
    val unreadNotifs = notifications.count { !it.isRead }

    val navItems = listOf(
        Triple(MainTab.HOME, strings.navHome, Icons.Rounded.Home),
        Triple(MainTab.SERVICES, strings.navServices, Icons.Rounded.ElectricalServices),
        Triple(MainTab.MESSAGES, strings.navMessages, Icons.AutoMirrored.Rounded.Chat),
        Triple(MainTab.BOOKINGS, strings.navBookings, Icons.Rounded.CalendarMonth),
        Triple(MainTab.PROFILE, strings.navProfile, Icons.Rounded.Person)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = Color.White) {
                        navItems.forEach { (tab, label, icon) ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = {
                                    NavTabIcon(
                                        icon = icon,
                                        label = label,
                                        badgeCount = if (tab == MainTab.MESSAGES) unreadChats else 0
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepNavy,
                                    selectedTextColor = DeepNavy,
                                    indicatorColor = ElectricBlueSoft,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = Color.White,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(Modifier.height(12.dp))
                        navItems.forEach { (tab, label, icon) ->
                            NavigationRailItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = {
                                    NavTabIcon(
                                        icon = icon,
                                        label = label,
                                        badgeCount = if (tab == MainTab.MESSAGES) unreadChats else 0
                                    )
                                },
                                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = DeepNavy,
                                    selectedTextColor = DeepNavy,
                                    indicatorColor = ElectricBlueSoft
                                ),
                                modifier = Modifier.testTag("rail_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (currentTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                customerName = profile.fullName,
                                businessSettings = businessSettings,
                                categories = categories,
                                services = services,
                                recentBookings = bookings,
                                reviews = approvedReviews,
                                unreadNotificationCount = unreadNotifs,
                                searchQuery = homeSearchQuery,
                                onSearchQueryChange = { homeSearchQuery = it },
                                onToggleLanguage = viewModel::toggleLanguage,
                                onOpenNotifications = { secondaryStack.add(SecondaryRoute.Notifications) },
                                onOpenAllServices = { catId ->
                                    servicesFilterCategoryId = catId
                                    currentTab = MainTab.SERVICES
                                },
                                onOpenServiceDetail = { svc ->
                                    secondaryStack.add(SecondaryRoute.ServiceDetail(svc.id))
                                },
                                onOpenBookingForm = { svc ->
                                    secondaryStack.add(SecondaryRoute.BookingForm(svc.id))
                                },
                                onOpenContactUs = { secondaryStack.add(SecondaryRoute.ContactUs) },
                                onOpenMessages = { currentTab = MainTab.MESSAGES },
                                onOpenMyBookings = { currentTab = MainTab.BOOKINGS },
                                isAdmin = isAdmin,
                                onOpenAdminConsole = onOpenAdminRoot
                            )
                        }

                        MainTab.SERVICES -> {
                            ServicesCatalogScreen(
                                categories = categories,
                                services = services,
                                initialCategoryId = servicesFilterCategoryId,
                                onOpenServiceDetail = { svc ->
                                    secondaryStack.add(SecondaryRoute.ServiceDetail(svc.id))
                                },
                                onBookService = { svc ->
                                    secondaryStack.add(SecondaryRoute.BookingForm(svc.id))
                                },
                                onContactUs = { secondaryStack.add(SecondaryRoute.ContactUs) }
                            )
                        }

                        MainTab.MESSAGES -> {
                            MessagesScreen(
                                messages = messages,
                                businessSettings = businessSettings,
                                onMarkRead = viewModel::markMyMessagesRead,
                                onSendMessage = { text, imageUri, onSent ->
                                    viewModel.sendCustomerMessage(text, imageUri, onSent)
                                }
                            )
                        }

                        MainTab.BOOKINGS -> {
                            MyBookingsScreen(
                                bookings = bookings,
                                isBusy = isBusy,
                                onCancelBooking = viewModel::cancelBooking,
                                onSubmitReview = { booking, rating, comment, onDone ->
                                    viewModel.submitReview(booking, rating, comment, onDone)
                                }
                            )
                        }

                        MainTab.PROFILE -> {
                            ProfileScreen(
                                userProfile = profile,
                                isAdmin = isAdmin,
                                notificationsEnabled = notificationsEnabled,
                                isBusy = isBusy,
                                onToggleLanguage = viewModel::toggleLanguage,
                                onToggleNotifications = viewModel::setNotificationsEnabled,
                                onUpdateProfile = { fullName, username, phone, address, profileImage, onDone ->
                                    viewModel.updateProfile(
                                        existing = profile,
                                        fullName = fullName,
                                        username = username,
                                        phone = phone,
                                        address = address,
                                        profileImage = profileImage,
                                        onSuccess = onDone
                                    )
                                },
                                onOpenMyBookings = { currentTab = MainTab.BOOKINGS },
                                onOpenMyMessages = { currentTab = MainTab.MESSAGES },
                                onOpenContactUs = { secondaryStack.add(SecondaryRoute.ContactUs) },
                                onOpenPrivacyPolicy = { secondaryStack.add(SecondaryRoute.PrivacyPolicy) },
                                onOpenTerms = { secondaryStack.add(SecondaryRoute.Terms) },
                                onOpenAdminConsole = onOpenAdminRoot,
                                onLogout = onSignOut,
                                onDeleteAccount = {
                                    viewModel.deleteAccount(profile.username) {
                                        onSignOut()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FirebaseUnconfiguredScreen(
    isBangla: Boolean,
    onToggleLanguage: () -> Unit
) {
    val strings = LocalAppStrings.current
    val categories = remember { DefaultCatalogData.defaultCategoryModels() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            color = DeepNavy,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandLogoMark(sizeDp = 48)
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text(
                                text = strings.brandName,
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldenYellow
                            )
                            Text(
                                text = strings.tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                    FilterChip(
                        selected = false,
                        onClick = onToggleLanguage,
                        label = { Text(strings.switchLangAction, color = Color.White) }
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isBangla) {
                        "Firebase ক্লাউড কনফিগারেশন যুক্ত নেই"
                    } else {
                        "Firebase Cloud Config Not Bundled"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepNavy
                )
                Text(
                    text = if (isBangla) {
                        "এই APK-টি GitHub থেকে google-services.json ছাড়াই তৈরি হয়েছে, তাই লগইন ও ক্লাউড চ্যাট চালু করতে দুটি উপায়ের একটি অনুসরণ করুন:\n১) AI Studio -> Settings -> Download/Generate APK থেকে সম্পূর্ণ APK ডাউনলোড করুন।\n২) অথবা GitHub Repository Secrets-এ GOOGLE_SERVICES_JSON যোগ করে পুনরায় APK বিল্ড করুন।"
                    } else {
                        "This APK was built on GitHub without google-services.json. To enable Google Sign-In, live chat, and cloud bookings:\n1) Download the APK directly from AI Studio Settings -> Generate APK.\n2) Or add GOOGLE_SERVICES_JSON to GitHub Repository Secrets and rebuild."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepNavy
                )
            }
        }

        Text(
            text = strings.allServices,
            style = MaterialTheme.typography.titleMedium,
            color = DeepNavy
        )
        categories.forEach { cat ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = cat.localizedName(isBangla),
                        style = MaterialTheme.typography.titleSmall,
                        color = DeepNavy
                    )
                    Text(
                        text = cat.localizedDescription(isBangla),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NavTabIcon(
    icon: ImageVector,
    label: String,
    badgeCount: Int
) {
    if (badgeCount > 0) {
        BadgedBox(
            badge = {
                Badge(containerColor = ElectricBlue, contentColor = Color.White) {
                    Text(badgeCount.toString())
                }
            }
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
    } else {
        Icon(imageVector = icon, contentDescription = label)
    }
}

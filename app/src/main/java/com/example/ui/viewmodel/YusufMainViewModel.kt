package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.unwrapFirestoreRootCause
import com.example.data.model.AppNotification
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
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.BookingRepository
import com.example.data.repository.BusinessAndReviewsRepository
import com.example.data.repository.CatalogRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserRepository
import com.example.data.seed.DefaultCatalogData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class YusufMainViewModel(
    private val currentUserId: String,
    private val currentUserEmail: String,
    private val currentUserName: String,
    private val isEmailVerified: Boolean,
    private val userRepository: UserRepository,
    private val catalogRepository: CatalogRepository,
    private val bookingRepository: BookingRepository,
    private val chatRepository: ChatRepository,
    private val businessRepository: BusinessAndReviewsRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    val language: StateFlow<AppLanguage> = preferencesRepository.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AppLanguage.BANGLA)

    val notificationsEnabled: StateFlow<Boolean> = preferencesRepository.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    val profileState: StateFlow<UiState<UserProfile?>> = userRepository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { err ->
            if (err is CancellationException && unwrapFirestoreRootCause(err) == err) throw err
            val root = unwrapFirestoreRootCause(err)
            Log.w(TAG, "Profile stream error", root)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val categoriesState: StateFlow<UiState<List<ServiceCategory>>> = catalogRepository.observeCategories()
        .map<List<ServiceCategory>, UiState<List<ServiceCategory>>> { list ->
            UiState.Success(list.ifEmpty { DefaultCatalogData.defaultCategoryModels() })
        }
        .catch { err ->
            if (err is CancellationException && unwrapFirestoreRootCause(err) == err) throw err
            val root = unwrapFirestoreRootCause(err)
            Log.w(TAG, "Categories stream error", root)
            emit(UiState.Success(DefaultCatalogData.defaultCategoryModels()))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            UiState.Success(DefaultCatalogData.defaultCategoryModels())
        )

    val servicesState: StateFlow<UiState<List<ServiceItem>>> = catalogRepository.observeServices()
        .map<List<ServiceItem>, UiState<List<ServiceItem>>> { list ->
            UiState.Success(list.ifEmpty { DefaultCatalogData.defaultServiceModels() })
        }
        .catch { err ->
            if (err is CancellationException && unwrapFirestoreRootCause(err) == err) throw err
            val root = unwrapFirestoreRootCause(err)
            Log.w(TAG, "Services stream error", root)
            emit(UiState.Success(DefaultCatalogData.defaultServiceModels()))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            UiState.Success(DefaultCatalogData.defaultServiceModels())
        )

    val myBookingsState: StateFlow<UiState<List<Booking>>> = bookingRepository.observeMyBookings(currentUserId)
        .map<List<Booking>, UiState<List<Booking>>> { UiState.Success(it) }
        .catch { err ->
            if (err is CancellationException && unwrapFirestoreRootCause(err) == err) throw err
            val root = unwrapFirestoreRootCause(err)
            Log.w(TAG, "Bookings stream error", root)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load bookings"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val myConversation: StateFlow<Conversation?> = chatRepository.observeConversation(currentUserId)
        .catch { err ->
            Log.w(TAG, "Conversation stream error", unwrapFirestoreRootCause(err))
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    val myMessagesState: StateFlow<UiState<List<ChatMessage>>> = chatRepository.observeMessages(currentUserId)
        .map<List<ChatMessage>, UiState<List<ChatMessage>>> { UiState.Success(it) }
        .catch { err ->
            if (err is CancellationException && unwrapFirestoreRootCause(err) == err) throw err
            val root = unwrapFirestoreRootCause(err)
            Log.w(TAG, "Messages stream error", root)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load messages"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val businessSettings: StateFlow<BusinessSettings> = businessRepository.observeBusinessSettings()
        .catch { err ->
            Log.w(TAG, "Business settings stream error", unwrapFirestoreRootCause(err))
            emit(BusinessSettings())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), BusinessSettings())

    val approvedReviews: StateFlow<List<ServiceReview>> = businessRepository.observeApprovedReviews()
        .catch { err ->
            Log.w(TAG, "Reviews stream error", unwrapFirestoreRootCause(err))
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val notifications: StateFlow<List<AppNotification>> = businessRepository.observeNotifications(currentUserId)
        .catch { err ->
            Log.w(TAG, "Notifications stream error", unwrapFirestoreRootCause(err))
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    // --- Admin Streams (only active when verified admin) ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val adminAllBookings: StateFlow<List<Booking>> = _isAdmin
        .flatMapLatest { admin ->
            if (admin) bookingRepository.observeAllBookingsByAdmin().catch { emit(emptyList()) }
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminAllConversations: StateFlow<List<Conversation>> = _isAdmin
        .flatMapLatest { admin ->
            if (admin) chatRepository.observeAllConversationsByAdmin().catch { emit(emptyList()) }
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminAllCustomers: StateFlow<List<UserProfile>> = _isAdmin
        .flatMapLatest { admin ->
            if (admin) userRepository.observeAllCustomers().catch { emit(emptyList()) }
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminAllReviews: StateFlow<List<ServiceReview>> = _isAdmin
        .flatMapLatest { admin ->
            if (admin) businessRepository.observeAllReviewsByAdmin().catch { emit(emptyList()) }
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _selectedAdminConversationId = MutableStateFlow<String?>(null)
    val selectedAdminConversationId: StateFlow<String?> = _selectedAdminConversationId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminActiveMessages: StateFlow<List<ChatMessage>> = _selectedAdminConversationId
        .flatMapLatest { convId ->
            if (!convId.isNullOrBlank() && _isAdmin.value) {
                chatRepository.observeMessages(convId).catch { emit(emptyList()) }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    init {
        viewModelScope.launch {
            val admin = userRepository.checkIsAdmin(currentUserId, currentUserEmail, isEmailVerified)
            _isAdmin.value = admin
            if (admin) {
                try {
                    catalogRepository.seedDefaultCatalogIfEmptyByAdmin()
                } catch (e: Exception) {
                    Log.w(TAG, "Auto-seed catalog failed", e)
                }
            }
        }
    }

    fun clearFeedback() {
        _actionError.value = null
        _actionMessage.value = null
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val next = if (language.value == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
            preferencesRepository.setLanguage(next)
            if ((profileState.value as? UiState.Success)?.data != null) {
                try {
                    userRepository.updatePreferences(preferredLanguage = next.code)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
            if ((profileState.value as? UiState.Success)?.data != null) {
                try {
                    userRepository.updatePreferences(notificationsEnabled = enabled)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun createProfile(
        fullName: String,
        username: String,
        phone: String,
        address: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            _actionError.value = null
            try {
                userRepository.createProfile(
                    fullName = fullName,
                    username = username,
                    email = currentUserEmail,
                    phone = phone,
                    address = address,
                    preferredLanguage = language.value.code
                )
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = e.message
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun updateProfile(
        existing: UserProfile,
        fullName: String,
        username: String,
        phone: String,
        address: String,
        profileImage: String = existing.profileImage,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            _actionError.value = null
            try {
                userRepository.updateProfile(
                    existingProfile = existing,
                    fullName = fullName,
                    username = username,
                    phone = phone,
                    address = address,
                    profileImage = profileImage
                )
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = e.message
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun submitBooking(
        service: ServiceItem,
        category: ServiceCategory?,
        customerName: String,
        phone: String,
        address: String,
        cityArea: String,
        description: String,
        imageUrls: List<String>,
        preferredDate: String,
        preferredTime: String,
        onBooked: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            _actionError.value = null
            try {
                val code = bookingRepository.submitBooking(
                    serviceId = service.id,
                    categoryId = service.categoryId,
                    serviceNameEn = service.nameEn,
                    serviceNameBn = service.nameBn,
                    categoryNameEn = category?.nameEn ?: "",
                    categoryNameBn = category?.nameBn ?: "",
                    customerName = customerName,
                    phone = phone,
                    address = address,
                    cityArea = cityArea,
                    description = description,
                    imageUrls = imageUrls,
                    preferredDate = preferredDate,
                    preferredTime = preferredTime
                )
                onBooked(code)
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Booking failed"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                bookingRepository.cancelMyBooking(bookingId)
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun sendCustomerMessage(text: String, imageDataUri: String = "", onSent: () -> Unit = {}) {
        if (text.isBlank() && imageDataUri.isBlank()) return
        viewModelScope.launch {
            val profile = (profileState.value as? UiState.Success)?.data
            try {
                chatRepository.sendCustomerMessage(
                    customerName = profile?.fullName?.ifBlank { currentUserName } ?: currentUserName,
                    customerUsername = profile?.username ?: "",
                    text = text,
                    imageDataUri = imageDataUri
                )
                onSent()
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            }
        }
    }

    fun markMyMessagesRead() {
        viewModelScope.launch {
            chatRepository.markConversationReadByCustomer(currentUserId)
        }
    }

    fun markNotificationRead(notification: AppNotification) {
        viewModelScope.launch {
            businessRepository.markNotificationRead(notification)
        }
    }

    fun submitReview(
        booking: Booking,
        rating: Long,
        comment: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                val profile = (profileState.value as? UiState.Success)?.data
                businessRepository.submitReview(
                    booking = booking,
                    customerName = profile?.fullName?.ifBlank { booking.customerName } ?: booking.customerName,
                    rating = rating,
                    comment = comment
                )
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun deleteAccount(username: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                userRepository.deleteAccountData(username)
                onDeleted()
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            } finally {
                _isBusy.value = false
            }
        }
    }

    // --- Admin Actions ---
    fun adminSeedDefaultCatalog() {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                catalogRepository.seedDefaultCatalogIfEmptyByAdmin()
                _actionMessage.value = "Default catalog synchronized."
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun adminSaveCategory(
        categoryId: String?,
        nameEn: String,
        nameBn: String,
        descriptionEn: String,
        descriptionBn: String,
        iconKey: String,
        status: String,
        sortOrder: Long
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                catalogRepository.saveCategoryByAdmin(
                    categoryId, nameEn, nameBn, descriptionEn, descriptionBn, iconKey, status, sortOrder
                )
                _actionMessage.value = "Category saved."
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun adminDeleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                catalogRepository.deleteCategoryByAdmin(categoryId)
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            }
        }
    }

    fun adminSaveService(
        serviceId: String?,
        categoryId: String,
        nameEn: String,
        nameBn: String,
        descriptionEn: String,
        descriptionBn: String,
        featuresEn: List<String>,
        featuresBn: List<String>,
        price: Double,
        priceDisplayEn: String,
        priceDisplayBn: String,
        availability: String,
        isPopular: Boolean
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                catalogRepository.saveServiceByAdmin(
                    serviceId, categoryId, nameEn, nameBn, descriptionEn, descriptionBn,
                    featuresEn, featuresBn, price, priceDisplayEn, priceDisplayBn, availability, isPopular
                )
                _actionMessage.value = "Service saved."
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun adminDeleteService(serviceId: String) {
        viewModelScope.launch {
            try {
                catalogRepository.deleteServiceByAdmin(serviceId)
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            }
        }
    }

    fun adminUpdateBookingStatus(booking: Booking, status: BookingStatus, notes: String) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                bookingRepository.updateBookingStatusByAdmin(booking, status, notes)
                _actionMessage.value = "Booking ${booking.bookingCode} updated."
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun adminSelectConversation(conversationId: String?) {
        _selectedAdminConversationId.value = conversationId
        if (!conversationId.isNullOrBlank()) {
            viewModelScope.launch {
                chatRepository.markConversationReadByAdmin(conversationId)
            }
        }
    }

    fun adminSendReply(customerId: String, text: String, imageDataUri: String = "") {
        if (text.isBlank() && imageDataUri.isBlank()) return
        viewModelScope.launch {
            try {
                chatRepository.sendAdminReply(customerId, text, imageDataUri)
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            }
        }
    }

    fun adminUpdateBusinessSettings(
        businessName: String,
        phoneNumber: String,
        whatsappNumber: String,
        email: String,
        addressEn: String,
        addressBn: String,
        mapsQueryUrl: String,
        workingHoursEn: String,
        workingHoursBn: String,
        availabilityStatus: BusinessAvailability,
        serviceAreasEn: String,
        serviceAreasBn: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                businessRepository.updateBusinessSettingsByAdmin(
                    businessName, phoneNumber, whatsappNumber, email,
                    addressEn, addressBn, mapsQueryUrl, workingHoursEn, workingHoursBn,
                    availabilityStatus, serviceAreasEn, serviceAreasBn
                )
                _actionMessage.value = "Business settings updated."
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun adminModerateReview(reviewId: String, approvalStatus: String) {
        viewModelScope.launch {
            try {
                businessRepository.moderateReviewByAdmin(reviewId, approvalStatus)
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            }
        }
    }

    fun adminSendAnnouncement(titleEn: String, titleBn: String, bodyEn: String, bodyBn: String) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                businessRepository.sendNotificationByAdmin(
                    targetUserId = "all",
                    titleEn = titleEn,
                    titleBn = titleBn,
                    bodyEn = bodyEn,
                    bodyBn = bodyBn,
                    type = "announcement"
                )
                _actionMessage.value = "Announcement sent to all customers."
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun adminUpdateCustomerStatus(targetUserId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                userRepository.updateCustomerAccountStatusByAdmin(targetUserId, newStatus)
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage
            }
        }
    }

    val defaultDisplayName: String get() = currentUserName
    val defaultEmail: String get() = currentUserEmail

    private companion object {
        const val TAG = "YusufMainVM"
        const val STOP_TIMEOUT_MS = 5000L
    }
}

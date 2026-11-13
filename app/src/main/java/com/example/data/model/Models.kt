package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImage: String = "",
    val address: String = "",
    val role: String = "customer",
    val accountStatus: String = "active",
    val preferredLanguage: String = "bn",
    val notificationsEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AdminUser(
    @DocumentId val id: String = "",
    val adminId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "admin",
    val permissions: List<String> = emptyList(),
    val status: String = "active",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ServiceCategory(
    @DocumentId val id: String = "",
    val nameEn: String = "",
    val nameBn: String = "",
    val descriptionEn: String = "",
    val descriptionBn: String = "",
    val iconKey: String = "electrical",
    val imageUrl: String = "",
    val status: String = "active",
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun localizedName(isBangla: Boolean): String = if (isBangla && nameBn.isNotBlank()) nameBn else nameEn
    fun localizedDescription(isBangla: Boolean): String = if (isBangla && descriptionBn.isNotBlank()) descriptionBn else descriptionEn
}

@IgnoreExtraProperties
data class ServiceItem(
    @DocumentId val id: String = "",
    val categoryId: String = "",
    val nameEn: String = "",
    val nameBn: String = "",
    val descriptionEn: String = "",
    val descriptionBn: String = "",
    val featuresEn: List<String> = emptyList(),
    val featuresBn: List<String> = emptyList(),
    val imageUrl: String = "",
    val price: Double = 0.0,
    val priceType: String = "estimate",
    val priceDisplayEn: String = "",
    val priceDisplayBn: String = "",
    val availability: String = "available",
    val isPopular: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun localizedName(isBangla: Boolean): String = if (isBangla && nameBn.isNotBlank()) nameBn else nameEn
    fun localizedDescription(isBangla: Boolean): String = if (isBangla && descriptionBn.isNotBlank()) descriptionBn else descriptionEn
    fun localizedFeatures(isBangla: Boolean): List<String> = if (isBangla && featuresBn.isNotEmpty()) featuresBn else featuresEn
    fun localizedPrice(isBangla: Boolean): String = if (isBangla && priceDisplayBn.isNotBlank()) priceDisplayBn else priceDisplayEn
}

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerUsername: String = "",
    val assignedAdminId: String = "",
    val lastMessage: String = "",
    @ServerTimestamp val lastMessageAt: Timestamp? = null,
    val unreadByCustomer: Long = 0L,
    val unreadByAdmin: Long = 0L,
    val status: String = "open",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val customerId: String = "",
    val senderId: String = "",
    val senderRole: String = "customer",
    val messageType: String = "text",
    val text: String = "",
    val imageUrl: String = "",
    @ServerTimestamp val sentAt: Timestamp? = null,
    val readAt: Timestamp? = null
)

enum class BookingStatus(val wireValue: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    SCHEDULED("scheduled"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    companion object {
        fun fromWire(value: String): BookingStatus =
            entries.firstOrNull { it.wireValue == value } ?: PENDING
    }
}

@IgnoreExtraProperties
data class Booking(
    @DocumentId val id: String = "",
    val bookingCode: String = "",
    val customerId: String = "",
    val serviceId: String = "",
    val categoryId: String = "",
    val serviceNameEn: String = "",
    val serviceNameBn: String = "",
    val categoryNameEn: String = "",
    val categoryNameBn: String = "",
    val customerName: String = "",
    val phone: String = "",
    val address: String = "",
    val cityArea: String = "",
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val preferredDate: String = "",
    val preferredTime: String = "",
    val bookingStatus: String = BookingStatus.PENDING.wireValue,
    val adminNotes: String = "",
    val hasReview: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun statusEnum(): BookingStatus = BookingStatus.fromWire(bookingStatus)
    fun localizedServiceName(isBangla: Boolean): String = if (isBangla && serviceNameBn.isNotBlank()) serviceNameBn else serviceNameEn
    fun localizedCategoryName(isBangla: Boolean): String = if (isBangla && categoryNameBn.isNotBlank()) categoryNameBn else categoryNameEn
}

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val titleEn: String = "",
    val titleBn: String = "",
    val bodyEn: String = "",
    val bodyBn: String = "",
    val type: String = "booking",
    val relatedId: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun localizedTitle(isBangla: Boolean): String = if (isBangla && titleBn.isNotBlank()) titleBn else titleEn
    fun localizedBody(isBangla: Boolean): String = if (isBangla && bodyBn.isNotBlank()) bodyBn else bodyEn
}

@IgnoreExtraProperties
data class ServiceReview(
    @DocumentId val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val bookingId: String = "",
    val serviceId: String = "",
    val serviceNameEn: String = "",
    val serviceNameBn: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    val approvalStatus: String = "approved",
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class BusinessAvailability(val wireValue: String) {
    ONLINE("online"),
    BUSY("busy"),
    OFFLINE("offline");

    companion object {
        fun fromWire(value: String): BusinessAvailability =
            entries.firstOrNull { it.wireValue == value } ?: ONLINE
    }
}

@IgnoreExtraProperties
data class BusinessSettings(
    @DocumentId val id: String = "main",
    val businessName: String = "Yusuf Electronics",
    val taglineEn: String = "Complete Electrical, AC, CCTV & Home Services",
    val taglineBn: String = "সম্পূর্ণ ইলেকট্রিক্যাল, এসি, সিসিটিভি ও হোম সার্ভিস",
    val phoneNumber: String = "",
    val whatsappNumber: String = "",
    val email: String = "",
    val addressEn: String = "",
    val addressBn: String = "",
    val mapsQueryUrl: String = "",
    val workingHoursEn: String = "9:00 AM - 9:00 PM",
    val workingHoursBn: String = "সকাল ৯:০০ - রাত ৯:০০",
    val availabilityStatus: String = BusinessAvailability.ONLINE.wireValue,
    val serviceAreasEn: String = "Residential & Commercial Areas",
    val serviceAreasBn: String = "আবাসিক ও বাণিজ্যিক এলাকা",
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun availabilityEnum(): BusinessAvailability = BusinessAvailability.fromWire(availabilityStatus)
}

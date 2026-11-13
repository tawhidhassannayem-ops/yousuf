package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.AppNotification
import com.example.data.model.Booking
import com.example.data.model.BusinessAvailability
import com.example.data.model.BusinessSettings
import com.example.data.model.ServiceReview
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BusinessAndReviewsRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in.")

    fun observeBusinessSettings(): Flow<BusinessSettings> =
        db.collection("business_settings").document("main")
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(BusinessSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: BusinessSettings()
                } else {
                    BusinessSettings()
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.GET, "business_settings/main")
                }
                throw error
            }

    suspend fun updateBusinessSettingsByAdmin(
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
        val uid = requireUserId()
        val ref = db.collection("business_settings").document("main")
        try {
            val payload = mapOf(
                "businessName" to businessName.trim().ifBlank { "Yusuf Electronics" },
                "taglineEn" to "Complete Electrical, AC, CCTV & Home Services",
                "taglineBn" to "সম্পূর্ণ ইলেকট্রিক্যাল, এসি, সিসিটিভি ও হোম সার্ভিস",
                "phoneNumber" to phoneNumber.trim(),
                "whatsappNumber" to whatsappNumber.trim(),
                "email" to email.trim(),
                "addressEn" to addressEn.trim(),
                "addressBn" to addressBn.trim(),
                "mapsQueryUrl" to mapsQueryUrl.trim(),
                "workingHoursEn" to workingHoursEn.trim(),
                "workingHoursBn" to workingHoursBn.trim(),
                "availabilityStatus" to availabilityStatus.wireValue,
                "serviceAreasEn" to serviceAreasEn.trim(),
                "serviceAreasBn" to serviceAreasBn.trim(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val batch = db.batch()
            batch.set(ref, payload)
            batch.set(
                db.collection("audit_logs").document(),
                mapOf(
                    "actorId" to uid,
                    "actorEmail" to (auth.currentUser?.email ?: ""),
                    "action" to "UPDATE_BUSINESS_SETTINGS",
                    "targetCollection" to "business_settings",
                    "targetId" to "main",
                    "details" to "Status=${availabilityStatus.wireValue}",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            throw e
        }
    }

    fun observeApprovedReviews(): Flow<List<ServiceReview>> =
        db.collection("reviews")
            .whereEqualTo("approvalStatus", "approved")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ServiceReview::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "reviews")
                }
                throw error
            }

    fun observeAllReviewsByAdmin(): Flow<List<ServiceReview>> =
        db.collection("reviews")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ServiceReview::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.LIST, "reviews")
                throw error
            }

    suspend fun submitReview(
        booking: Booking,
        customerName: String,
        rating: Long,
        comment: String
    ) {
        val uid = requireUserId()
        val reviewRef = db.collection("reviews").document()
        val bookingRef = db.collection("bookings").document(booking.id)
        try {
            val batch = db.batch()
            batch.set(
                reviewRef,
                mapOf(
                    "customerId" to uid,
                    "customerName" to customerName.ifBlank { booking.customerName },
                    "bookingId" to booking.id,
                    "serviceId" to booking.serviceId,
                    "serviceNameEn" to booking.serviceNameEn,
                    "serviceNameBn" to booking.serviceNameBn,
                    "rating" to rating.coerceIn(1L, 5L),
                    "comment" to comment.trim(),
                    "approvalStatus" to "approved",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                bookingRef,
                mapOf(
                    "hasReview" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, reviewRef.path)
            throw e
        }
    }

    suspend fun moderateReviewByAdmin(reviewId: String, approvalStatus: String) {
        val uid = requireUserId()
        val ref = db.collection("reviews").document(reviewId)
        try {
            val batch = db.batch()
            batch.update(ref, "approvalStatus", approvalStatus)
            batch.set(
                db.collection("audit_logs").document(),
                mapOf(
                    "actorId" to uid,
                    "actorEmail" to (auth.currentUser?.email ?: ""),
                    "action" to "MODERATE_REVIEW",
                    "targetCollection" to "reviews",
                    "targetId" to reviewId,
                    "details" to "Status=$approvalStatus",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            throw e
        }
    }

    fun observeNotifications(userId: String): Flow<List<AppNotification>> {
        val personalFlow = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { it.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "notifications")
                }
                throw error
            }

        val announcementFlow = db.collection("notifications")
            .whereEqualTo("userId", "all")
            .snapshots()
            .map { it.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "notifications")
                }
                throw error
            }

        return combine(personalFlow, announcementFlow) { personal, global ->
            (personal + global).sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
    }

    suspend fun markNotificationRead(notification: AppNotification) {
        val uid = requireUserId()
        if (notification.userId != uid || notification.isRead) return
        val ref = db.collection("notifications").document(notification.id)
        try {
            ref.update("isRead", true).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
        }
    }

    suspend fun sendNotificationByAdmin(
        targetUserId: String,
        titleEn: String,
        titleBn: String,
        bodyEn: String,
        bodyBn: String,
        type: String = "announcement"
    ) {
        val uid = requireUserId()
        val ref = db.collection("notifications").document()
        try {
            val batch = db.batch()
            batch.set(
                ref,
                mapOf(
                    "userId" to targetUserId,
                    "titleEn" to titleEn.trim(),
                    "titleBn" to titleBn.trim().ifBlank { titleEn.trim() },
                    "bodyEn" to bodyEn.trim(),
                    "bodyBn" to bodyBn.trim().ifBlank { bodyEn.trim() },
                    "type" to type,
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                db.collection("audit_logs").document(),
                mapOf(
                    "actorId" to uid,
                    "actorEmail" to (auth.currentUser?.email ?: ""),
                    "action" to "SEND_NOTIFICATION",
                    "targetCollection" to "notifications",
                    "targetId" to ref.id,
                    "details" to "Target=$targetUserId",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            throw e
        }
    }
}

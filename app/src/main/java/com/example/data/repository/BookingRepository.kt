package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class BookingRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in.")

    fun observeMyBookings(userId: String): Flow<List<Booking>> =
        db.collection("bookings")
            .whereEqualTo("customerId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Booking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "bookings")
                }
                throw error
            }

    fun observeAllBookingsByAdmin(): Flow<List<Booking>> =
        db.collection("bookings")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Booking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.LIST, "bookings")
                throw error
            }

    suspend fun getBookingById(bookingId: String): Booking? {
        val docRef = db.collection("bookings").document(bookingId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(Booking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun submitBooking(
        serviceId: String,
        categoryId: String,
        serviceNameEn: String,
        serviceNameBn: String,
        categoryNameEn: String,
        categoryNameBn: String,
        customerName: String,
        phone: String,
        address: String,
        cityArea: String,
        description: String,
        imageUrls: List<String>,
        preferredDate: String,
        preferredTime: String
    ): String {
        val uid = requireUserId()
        val bookingRef = db.collection("bookings").document()
        val notifRef = db.collection("notifications").document()
        val dateStamp = SimpleDateFormat("yyMMdd", Locale.US).format(Date())
        val suffix = Random.nextInt(1000, 9999)
        val bookingCode = "YE-$dateStamp-$suffix"

        val payload = mapOf(
            "bookingCode" to bookingCode,
            "customerId" to uid,
            "serviceId" to serviceId,
            "categoryId" to categoryId,
            "serviceNameEn" to serviceNameEn,
            "serviceNameBn" to serviceNameBn,
            "categoryNameEn" to categoryNameEn,
            "categoryNameBn" to categoryNameBn,
            "customerName" to customerName.trim(),
            "phone" to phone.trim(),
            "address" to address.trim(),
            "cityArea" to cityArea.trim(),
            "description" to description.trim(),
            "imageUrls" to imageUrls.take(3),
            "preferredDate" to preferredDate.trim(),
            "preferredTime" to preferredTime.trim(),
            "bookingStatus" to BookingStatus.PENDING.wireValue,
            "hasReview" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val notifPayload = mapOf(
            "userId" to uid,
            "titleEn" to "Booking Request Received ($bookingCode)",
            "titleBn" to "বুকিং অনুরোধ গৃহীত হয়েছে ($bookingCode)",
            "bodyEn" to "Your booking for $serviceNameEn is pending admin confirmation.",
            "bodyBn" to "$serviceNameBn-এর জন্য আপনার বুকিং অ্যাডমিনের অনুমোদনের অপেক্ষায় রয়েছে।",
            "type" to "booking",
            "relatedId" to bookingRef.id,
            "isRead" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            val batch = db.batch()
            batch.set(bookingRef, payload)
            batch.set(notifRef, notifPayload)
            batch.commit().await()
            bookingCode
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, bookingRef.path)
            throw e
        }
    }

    suspend fun cancelMyBooking(bookingId: String) {
        requireUserId()
        val docRef = db.collection("bookings").document(bookingId)
        try {
            docRef.update(
                mapOf(
                    "bookingStatus" to BookingStatus.CANCELLED.wireValue,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateBookingStatusByAdmin(
        booking: Booking,
        newStatus: BookingStatus,
        adminNotes: String
    ) {
        val adminUid = requireUserId()
        val docRef = db.collection("bookings").document(booking.id)
        val notifRef = db.collection("notifications").document()
        val auditRef = db.collection("audit_logs").document()

        val statusLabelEn = when (newStatus) {
            BookingStatus.PENDING -> "Pending"
            BookingStatus.ACCEPTED -> "Accepted"
            BookingStatus.REJECTED -> "Rejected"
            BookingStatus.SCHEDULED -> "Scheduled"
            BookingStatus.IN_PROGRESS -> "In Progress"
            BookingStatus.COMPLETED -> "Completed"
            BookingStatus.CANCELLED -> "Cancelled"
        }
        val statusLabelBn = when (newStatus) {
            BookingStatus.PENDING -> "অপেক্ষমাণ"
            BookingStatus.ACCEPTED -> "গৃহীত"
            BookingStatus.REJECTED -> "বাতিলকৃত"
            BookingStatus.SCHEDULED -> "নির্ধারিত"
            BookingStatus.IN_PROGRESS -> "চলমান"
            BookingStatus.COMPLETED -> "সম্পন্ন"
            BookingStatus.CANCELLED -> "বাতিল"
        }

        try {
            val batch = db.batch()
            val updates = mutableMapOf<String, Any>(
                "bookingStatus" to newStatus.wireValue,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (adminNotes.isNotBlank()) {
                updates["adminNotes"] = adminNotes.trim()
            }
            batch.update(docRef, updates)
            batch.set(
                notifRef,
                mapOf(
                    "userId" to booking.customerId,
                    "titleEn" to "Booking ${booking.bookingCode}: $statusLabelEn",
                    "titleBn" to "বুকিং ${booking.bookingCode}: $statusLabelBn",
                    "bodyEn" to "Your ${booking.serviceNameEn} booking is now $statusLabelEn." +
                        if (adminNotes.isNotBlank()) " Note: ${adminNotes.trim()}" else "",
                    "bodyBn" to "আপনার ${booking.serviceNameBn} বুকিং এখন $statusLabelBn।" +
                        if (adminNotes.isNotBlank()) " নোট: ${adminNotes.trim()}" else "",
                    "type" to "booking",
                    "relatedId" to booking.id,
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                auditRef,
                mapOf(
                    "actorId" to adminUid,
                    "actorEmail" to (auth.currentUser?.email ?: ""),
                    "action" to "UPDATE_BOOKING_STATUS",
                    "targetCollection" to "bookings",
                    "targetId" to booking.id,
                    "details" to "${booking.bookingCode} -> ${newStatus.wireValue}",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }
}

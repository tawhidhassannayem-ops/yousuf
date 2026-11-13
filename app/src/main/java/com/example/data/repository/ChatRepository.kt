package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
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

class ChatRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in.")

    fun observeConversation(conversationId: String): Flow<Conversation?> =
        db.collection("conversations").document(conversationId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(Conversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.GET, "conversations/$conversationId")
                }
                throw error
            }

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> =
        db.collection("conversations").document(conversationId)
            .collection("messages")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.sentAt?.toDate()?.time ?: Long.MAX_VALUE }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(
                        firestoreError ?: error,
                        OperationType.LIST,
                        "conversations/$conversationId/messages"
                    )
                }
                throw error
            }

    fun observeAllConversationsByAdmin(): Flow<List<Conversation>> =
        db.collection("conversations")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Conversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.lastMessageAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.LIST, "conversations")
                throw error
            }

    suspend fun sendCustomerMessage(
        customerName: String,
        customerUsername: String,
        text: String,
        imageDataUri: String = ""
    ) {
        val uid = requireUserId()
        val convRef = db.collection("conversations").document(uid)
        val msgRef = convRef.collection("messages").document()
        val msgType = if (imageDataUri.isNotBlank()) "image" else "text"
        val preview = if (text.isNotBlank()) text.trim() else "Photo attachment"

        try {
            val existingConv = convRef.get().await()
            val batch = db.batch()
            if (!existingConv.exists()) {
                val convPayload = mutableMapOf<String, Any>(
                    "customerId" to uid,
                    "customerName" to customerName.ifBlank { "Customer" },
                    "lastMessage" to preview.take(200),
                    "lastMessageAt" to FieldValue.serverTimestamp(),
                    "unreadByCustomer" to 0L,
                    "unreadByAdmin" to 1L,
                    "status" to "open",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                if (customerUsername.isNotBlank()) {
                    convPayload["customerUsername"] = customerUsername
                }
                batch.set(convRef, convPayload)
            } else {
                val updates = mutableMapOf<String, Any>(
                    "customerName" to customerName.ifBlank { "Customer" },
                    "lastMessage" to preview.take(200),
                    "lastMessageAt" to FieldValue.serverTimestamp(),
                    "unreadByAdmin" to FieldValue.increment(1L)
                )
                if (customerUsername.isNotBlank()) {
                    updates["customerUsername"] = customerUsername
                }
                batch.update(convRef, updates)
            }

            val msgPayload = mutableMapOf<String, Any>(
                "conversationId" to uid,
                "customerId" to uid,
                "senderId" to uid,
                "senderRole" to "customer",
                "messageType" to msgType,
                "text" to text.trim(),
                "sentAt" to FieldValue.serverTimestamp()
            )
            if (imageDataUri.isNotBlank()) {
                msgPayload["imageUrl"] = imageDataUri
            }
            batch.set(msgRef, msgPayload)
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, msgRef.path)
            throw e
        }
    }

    suspend fun sendAdminReply(
        customerId: String,
        text: String,
        imageDataUri: String = ""
    ) {
        val adminUid = requireUserId()
        val convRef = db.collection("conversations").document(customerId)
        val msgRef = convRef.collection("messages").document()
        val notifRef = db.collection("notifications").document()
        val msgType = if (imageDataUri.isNotBlank()) "image" else "text"
        val preview = if (text.isNotBlank()) text.trim() else "Photo from Yusuf Electronics"

        try {
            val batch = db.batch()
            batch.update(
                convRef,
                mapOf(
                    "assignedAdminId" to adminUid,
                    "lastMessage" to preview.take(200),
                    "lastMessageAt" to FieldValue.serverTimestamp(),
                    "unreadByCustomer" to FieldValue.increment(1L),
                    "unreadByAdmin" to 0L
                )
            )
            val msgPayload = mutableMapOf<String, Any>(
                "conversationId" to customerId,
                "customerId" to customerId,
                "senderId" to adminUid,
                "senderRole" to "admin",
                "messageType" to msgType,
                "text" to text.trim(),
                "sentAt" to FieldValue.serverTimestamp()
            )
            if (imageDataUri.isNotBlank()) {
                msgPayload["imageUrl"] = imageDataUri
            }
            batch.set(msgRef, msgPayload)
            batch.set(
                notifRef,
                mapOf(
                    "userId" to customerId,
                    "titleEn" to "New Reply from Yusuf Electronics",
                    "titleBn" to "ইউসুফ ইলেকট্রনিক্স থেকে নতুন বার্তা",
                    "bodyEn" to preview.take(200),
                    "bodyBn" to preview.take(200),
                    "type" to "message",
                    "relatedId" to customerId,
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, msgRef.path)
            throw e
        }
    }

    suspend fun markConversationReadByCustomer(conversationId: String) {
        val uid = requireUserId()
        val convRef = db.collection("conversations").document(conversationId)
        try {
            val convSnap = convRef.get().await()
            if (convSnap.exists() && (convSnap.getLong("unreadByCustomer") ?: 0L) > 0L) {
                convRef.update("unreadByCustomer", 0L).await()
            }
            val unreadMsgs = convRef.collection("messages")
                .whereEqualTo("senderRole", "admin")
                .get()
                .await()
            val batch = db.batch()
            var hasUpdates = false
            unreadMsgs.documents.forEach { doc ->
                if (doc.getTimestamp("readAt") == null) {
                    batch.update(doc.reference, "readAt", FieldValue.serverTimestamp())
                    hasUpdates = true
                }
            }
            if (hasUpdates) batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (auth.currentUser?.uid == uid) {
                handleFirestoreError(e, OperationType.UPDATE, convRef.path)
            }
        }
    }

    suspend fun markConversationReadByAdmin(conversationId: String) {
        requireUserId()
        val convRef = db.collection("conversations").document(conversationId)
        try {
            convRef.update("unreadByAdmin", 0L).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path)
        }
    }
}

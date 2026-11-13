package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class UserRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in.")

    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.GET, "users/$userId")
                }
                throw error
            }

    suspend fun getUserProfile(userId: String): UserProfile? {
        val docRef = db.collection("users").document(userId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
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

    suspend fun isUsernameAvailable(username: String, currentUid: String): Boolean {
        val clean = username.trim().lowercase()
        if (clean.length < 3) return false
        val docRef = db.collection("usernames").document(clean)
        return try {
            val snap = docRef.get().await()
            !snap.exists() || snap.getString("userId") == currentUid
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createProfile(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        address: String,
        preferredLanguage: String
    ) {
        val uid = requireUserId()
        val cleanUsername = username.trim().lowercase()
        val userRef = db.collection("users").document(uid)
        val usernameRef = db.collection("usernames").document(cleanUsername)

        try {
            val existingClaim = usernameRef.get().await()
            if (existingClaim.exists() && existingClaim.getString("userId") != uid) {
                throw IllegalArgumentException("USERNAME_TAKEN")
            }

            val batch = db.batch()
            if (!existingClaim.exists()) {
                batch.set(
                    usernameRef,
                    mapOf(
                        "userId" to uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "fullName" to fullName.trim(),
                "username" to cleanUsername,
                "email" to email.trim(),
                "role" to "customer",
                "accountStatus" to "active",
                "preferredLanguage" to preferredLanguage,
                "notificationsEnabled" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (phone.isNotBlank()) payload["phone"] = phone.trim()
            if (address.isNotBlank()) payload["address"] = address.trim()

            batch.set(userRef, payload)
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e.message != "USERNAME_TAKEN") {
                handleFirestoreError(e, OperationType.CREATE, userRef.path)
            }
            throw e
        }
    }

    suspend fun updateProfile(
        existingProfile: UserProfile,
        fullName: String,
        username: String,
        phone: String,
        address: String,
        profileImage: String = existingProfile.profileImage
    ) {
        val uid = requireUserId()
        val cleanUsername = username.trim().lowercase()
        val oldUsername = existingProfile.username.trim().lowercase()
        val userRef = db.collection("users").document(uid)

        try {
            if (cleanUsername != oldUsername) {
                val newClaimRef = db.collection("usernames").document(cleanUsername)
                val snap = newClaimRef.get().await()
                if (snap.exists() && snap.getString("userId") != uid) {
                    throw IllegalArgumentException("USERNAME_TAKEN")
                }
                val batch = db.batch()
                batch.set(
                    newClaimRef,
                    mapOf(
                        "userId" to uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                if (oldUsername.isNotBlank()) {
                    batch.delete(db.collection("usernames").document(oldUsername))
                }
                batch.update(
                    userRef,
                    mapOf(
                        "fullName" to fullName.trim(),
                        "username" to cleanUsername,
                        "phone" to phone.trim(),
                        "address" to address.trim(),
                        "profileImage" to profileImage,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } else {
                userRef.update(
                    mapOf(
                        "fullName" to fullName.trim(),
                        "phone" to phone.trim(),
                        "address" to address.trim(),
                        "profileImage" to profileImage,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e.message != "USERNAME_TAKEN") {
                handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            }
            throw e
        }
    }

    suspend fun updatePreferences(preferredLanguage: String? = null, notificationsEnabled: Boolean? = null) {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
        if (preferredLanguage != null) updates["preferredLanguage"] = preferredLanguage
        if (notificationsEnabled != null) updates["notificationsEnabled"] = notificationsEnabled
        try {
            userRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            throw e
        }
    }

    suspend fun checkIsAdmin(userId: String, email: String?, isEmailVerified: Boolean): Boolean {
        if (isEmailVerified && email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)) {
            return true
        }
        val docRef = db.collection("admins").document(userId)
        return try {
            val snap = docRef.get().await()
            snap.exists() && snap.getString("status") == "active"
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteAccountData(username: String) {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        try {
            val batch = db.batch()
            batch.delete(userRef)
            if (username.isNotBlank()) {
                batch.delete(db.collection("usernames").document(username.trim().lowercase()))
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, userRef.path)
            throw e
        }
    }

    fun observeAllCustomers(): Flow<List<UserProfile>> =
        db.collection("users")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val root = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (root == null && error is CancellationException) throw error
                handleFirestoreError(root ?: error, OperationType.LIST, "users")
                throw error
            }

    suspend fun updateCustomerAccountStatusByAdmin(targetUserId: String, newStatus: String) {
        val uid = requireUserId()
        val userRef = db.collection("users").document(targetUserId)
        val auditRef = db.collection("audit_logs").document()
        try {
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "accountStatus" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                auditRef,
                mapOf(
                    "actorId" to uid,
                    "actorEmail" to (auth.currentUser?.email ?: ""),
                    "action" to "UPDATE_CUSTOMER_STATUS",
                    "targetCollection" to "users",
                    "targetId" to targetUserId,
                    "details" to "Changed status to $newStatus",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            throw e
        }
    }

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "tawhidhassannayem@gmail.com"
    }
}

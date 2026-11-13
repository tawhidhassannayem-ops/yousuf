package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceItem
import com.example.data.seed.DefaultCatalogData
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

class CatalogRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in.")

    fun observeCategories(): Flow<List<ServiceCategory>> =
        db.collection("service_categories")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ServiceCategory::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.sortOrder }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "service_categories")
                }
                throw error
            }

    fun observeServices(): Flow<List<ServiceItem>> =
        db.collection("services")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ServiceItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.nameEn }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "services")
                }
                throw error
            }

    suspend fun seedDefaultCatalogIfEmptyByAdmin() {
        val uid = requireUserId()
        try {
            val existingCats = db.collection("service_categories").limit(1).get().await()
            val existingServices = db.collection("services").limit(1).get().await()
            val pendingWrites = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Map<String, Any>>>()

            if (existingCats.isEmpty) {
                DefaultCatalogData.categories.forEach { cat ->
                    val ref = db.collection("service_categories").document(cat.id)
                    pendingWrites.add(
                        ref to mapOf(
                            "nameEn" to cat.nameEn,
                            "nameBn" to cat.nameBn,
                            "descriptionEn" to cat.descriptionEn,
                            "descriptionBn" to cat.descriptionBn,
                            "iconKey" to cat.iconKey,
                            "status" to "active",
                            "sortOrder" to cat.sortOrder,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }

            if (existingServices.isEmpty) {
                DefaultCatalogData.services.forEach { svc ->
                    val ref = db.collection("services").document(svc.id)
                    pendingWrites.add(
                        ref to mapOf(
                            "categoryId" to svc.categoryId,
                            "nameEn" to svc.nameEn,
                            "nameBn" to svc.nameBn,
                            "descriptionEn" to svc.descriptionEn,
                            "descriptionBn" to svc.descriptionBn,
                            "featuresEn" to svc.featuresEn,
                            "featuresBn" to svc.featuresBn,
                            "price" to svc.price,
                            "priceType" to svc.priceType,
                            "priceDisplayEn" to svc.priceDisplayEn,
                            "priceDisplayBn" to svc.priceDisplayBn,
                            "availability" to "available",
                            "isPopular" to svc.isPopular,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }

            val settingsRef = db.collection("business_settings").document("main")
            val settingsSnap = settingsRef.get().await()
            if (!settingsSnap.exists()) {
                pendingWrites.add(settingsRef to DefaultCatalogData.businessSettingsPayload())
            }

            if (pendingWrites.isNotEmpty()) {
                pendingWrites.add(
                    db.collection("audit_logs").document() to mapOf(
                        "actorId" to uid,
                        "actorEmail" to (auth.currentUser?.email ?: ""),
                        "action" to "SEED_DEFAULT_CATALOG",
                        "targetCollection" to "service_categories",
                        "targetId" to "default_13",
                        "details" to "Seeded 13 default categories, services, and business settings",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                pendingWrites.chunked(8).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { (ref, data) -> batch.set(ref, data) }
                    batch.commit().await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "service_categories")
            throw e
        }
    }

    suspend fun saveCategoryByAdmin(
        categoryId: String?,
        nameEn: String,
        nameBn: String,
        descriptionEn: String,
        descriptionBn: String,
        iconKey: String,
        status: String,
        sortOrder: Long
    ) {
        val uid = requireUserId()
        val isNewId = categoryId.isNullOrBlank()
        val docRef = if (isNewId) {
            db.collection("service_categories").document()
        } else {
            db.collection("service_categories").document(categoryId!!)
        }
        try {
            val isCreate = isNewId || !docRef.get().await().exists()
            val batch = db.batch()
            if (isCreate) {
                batch.set(
                    docRef,
                    mapOf(
                        "nameEn" to nameEn.trim(),
                        "nameBn" to nameBn.trim(),
                        "descriptionEn" to descriptionEn.trim(),
                        "descriptionBn" to descriptionBn.trim(),
                        "iconKey" to iconKey,
                        "status" to status,
                        "sortOrder" to sortOrder,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.update(
                    docRef,
                    mapOf(
                        "nameEn" to nameEn.trim(),
                        "nameBn" to nameBn.trim(),
                        "descriptionEn" to descriptionEn.trim(),
                        "descriptionBn" to descriptionBn.trim(),
                        "iconKey" to iconKey,
                        "status" to status,
                        "sortOrder" to sortOrder,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.set(
                db.collection("audit_logs").document(),
                mapOf(
                    "actorId" to uid,
                    "actorEmail" to (auth.currentUser?.email ?: ""),
                    "action" to if (isCreate) "CREATE_CATEGORY" else "UPDATE_CATEGORY",
                    "targetCollection" to "service_categories",
                    "targetId" to docRef.id,
                    "details" to nameEn.trim(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCategoryByAdmin(categoryId: String) {
        val uid = requireUserId()
        val docRef = db.collection("service_categories").document(categoryId)
        try {
            if (!docRef.get().await().exists()) return
            val batch = db.batch()
            batch.delete(docRef)
            batch.set(
                db.collection("audit_logs").document(),
                mapOf(
                    "actorId" to uid,
                    "actorEmail" to (auth.currentUser?.email ?: ""),
                    "action" to "DELETE_CATEGORY",
                    "targetCollection" to "service_categories",
                    "targetId" to categoryId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun saveServiceByAdmin(
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
        val uid = requireUserId()
        val isNewId = serviceId.isNullOrBlank()
        val docRef = if (isNewId) db.collection("services").document() else db.collection("services").document(serviceId!!)
        try {
            val isCreate = isNewId || !docRef.get().await().exists()
            val batch = db.batch()
            if (isCreate) {
                batch.set(
                    docRef,
                    mapOf(
                        "categoryId" to categoryId,
                        "nameEn" to nameEn.trim(),
                        "nameBn" to nameBn.trim(),
                        "descriptionEn" to descriptionEn.trim(),
                        "descriptionBn" to descriptionBn.trim(),
                        "featuresEn" to featuresEn,
                        "featuresBn" to featuresBn,
                        "price" to price,
                        "priceType" to "estimate",
                        "priceDisplayEn" to priceDisplayEn.trim(),
                        "priceDisplayBn" to priceDisplayBn.trim(),
                        "availability" to availability,
                        "isPopular" to isPopular,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.update(
                    docRef,
                    mapOf(
                        "categoryId" to categoryId,
                        "nameEn" to nameEn.trim(),
                        "nameBn" to nameBn.trim(),
                        "descriptionEn" to descriptionEn.trim(),
                        "descriptionBn" to descriptionBn.trim(),
                        "featuresEn" to featuresEn,
                        "featuresBn" to featuresBn,
                        "price" to price,
                        "priceDisplayEn" to priceDisplayEn.trim(),
                        "priceDisplayBn" to priceDisplayBn.trim(),
                        "availability" to availability,
                        "isPopular" to isPopular,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.set(
                db.collection("audit_logs").document(),
                mapOf(
                    "actorId" to uid,
                    "actorEmail" to (auth.currentUser?.email ?: ""),
                    "action" to if (isCreate) "CREATE_SERVICE" else "UPDATE_SERVICE",
                    "targetCollection" to "services",
                    "targetId" to docRef.id,
                    "details" to nameEn.trim(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteServiceByAdmin(serviceId: String) {
        val uid = requireUserId()
        val docRef = db.collection("services").document(serviceId)
        try {
            if (!docRef.get().await().exists()) return
            val batch = db.batch()
            batch.delete(docRef)
            batch.set(
                db.collection("audit_logs").document(),
                mapOf(
                    "actorId" to uid,
                    "actorEmail" to (auth.currentUser?.email ?: ""),
                    "action" to "DELETE_SERVICE",
                    "targetCollection" to "services",
                    "targetId" to serviceId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}

package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import com.example.data.seed.DefaultCatalogData
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class YusufRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun userProfile_createAndReadOwnProfile_succeeds() = runBlocking {
        val uid = signInTestUser("customer_${UUID.randomUUID()}@test.com")
        val repo = UserRepository(firestore)
        val uname = "user_${UUID.randomUUID().toString().take(8)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createProfile(
                fullName = "Rahim Uddin",
                username = uname,
                email = "rahim@test.com",
                phone = "01711000000",
                address = "Mirpur 10, Dhaka",
                preferredLanguage = "bn"
            )
        }

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getUserProfile(uid) }
        assertNotNull(fetched)
        assertEquals("Rahim Uddin", fetched?.fullName)
        assertEquals(uname, fetched?.username)
        assertEquals("customer", fetched?.role)
    }

    @Test
    fun bookingAndChat_ownerFlowAndCrossUserIsolation_enforced() = runBlocking {
        val aliceEmail = "alice_${UUID.randomUUID()}@test.com"
        val bobEmail = "bob_${UUID.randomUUID()}@test.com"

        val aliceUid = signInTestUser(aliceEmail)
        val bookingRepo = BookingRepository(firestore)
        val chatRepo = ChatRepository(firestore)

        val code = withTimeout(DEFAULT_TIMEOUT_MS) {
            bookingRepo.submitBooking(
                serviceId = "svc_ceiling_fan_repair",
                categoryId = "cat_fan",
                serviceNameEn = "Ceiling Fan Repair",
                serviceNameBn = "সিলিং ফ্যান মেরামত",
                categoryNameEn = "Fan Repair",
                categoryNameBn = "ফ্যান মেরামত",
                customerName = "Alice",
                phone = "01711111111",
                address = "Road 5, Banani",
                cityArea = "Banani",
                description = "Fan makes noise",
                imageUrls = emptyList(),
                preferredDate = "2026-11-20",
                preferredTime = "11:00 AM"
            )
        }
        assertTrue(code.startsWith("YE-"))

        val myBookings = withTimeout(FLOW_TIMEOUT_MS) {
            bookingRepo.observeMyBookings(aliceUid).first { it.any { b -> b.bookingCode == code } }
        }
        val createdBooking = myBookings.first { it.bookingCode == code }
        assertEquals("pending", createdBooking.bookingStatus)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            chatRepo.sendCustomerMessage(
                customerName = "Alice",
                customerUsername = "alice_test",
                text = "Hello Yusuf Electronics!"
            )
        }
        val messages = withTimeout(FLOW_TIMEOUT_MS) {
            chatRepo.observeMessages(aliceUid).first { it.isNotEmpty() }
        }
        assertEquals("Hello Yusuf Electronics!", messages.first().text)

        // Bob cannot read Alice's booking or messages
        signInTestUser(bobEmail)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) { bookingRepo.getBookingById(createdBooking.id) }
            fail("Expected PERMISSION_DENIED when Bob reads Alice's booking")
        } catch (e: Exception) {
            val ffe = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", ffe)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ffe?.code)
        }

        try {
            withTimeout(FLOW_TIMEOUT_MS) { chatRepo.observeMessages(aliceUid).first() }
            fail("Expected PERMISSION_DENIED when Bob listens to Alice's chat")
        } catch (e: Exception) {
            val ffe = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", ffe)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ffe?.code)
        }
    }

    @Test
    fun observeMyBookings_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        assertNull(auth.currentUser)
        val bookingRepo = BookingRepository(firestore)
        try {
            withTimeout(FLOW_TIMEOUT_MS) { bookingRepo.observeMyBookings("some_uid").first() }
            fail("Expected PERMISSION_DENIED for unauthenticated listener")
        } catch (e: Exception) {
            val ffe = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", ffe)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ffe?.code)
        }
    }

    @Test
    fun catalog_defaultModelsAndAdminSeeding_succeeds() = runBlocking {
        val defaultCats = DefaultCatalogData.defaultCategoryModels()
        val defaultSvcs = DefaultCatalogData.defaultServiceModels()
        assertEquals(13, defaultCats.size)
        assertEquals(16, defaultSvcs.size)
        assertTrue(defaultCats.all { cat -> defaultSvcs.any { it.categoryId == cat.id } })

        val customerEmail = "customer_${UUID.randomUUID()}@test.com"
        signInTestUser(customerEmail)
        val catalogRepo = CatalogRepository(firestore)

        // Non-admin customer cannot seed or modify catalog
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) { catalogRepo.seedDefaultCatalogIfEmptyByAdmin() }
            fail("Expected PERMISSION_DENIED when non-admin seeds catalog")
        } catch (e: Exception) {
            val ffe = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", ffe)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ffe?.code)
        }

        // Sign in as bootstrapped admin and mark emailVerified in Auth emulator via admin API
        val adminUid = signInTestUser(UserRepository.BOOTSTRAPPED_ADMIN_EMAIL)
        val url = URL("http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/projects/demo-no-project/accounts:update")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer owner")
        }
        conn.outputStream.use { out ->
            out.write("""{"localId":"$adminUid","emailVerified":true}""".toByteArray())
        }
        assertEquals(200, conn.responseCode)
        auth.signOut()
        signInTestUser(UserRepository.BOOTSTRAPPED_ADMIN_EMAIL)
        assertTrue(auth.currentUser?.isEmailVerified == true)

        withTimeout(DEFAULT_TIMEOUT_MS) { catalogRepo.seedDefaultCatalogIfEmptyByAdmin() }

        val seededCats = withTimeout(FLOW_TIMEOUT_MS) {
            catalogRepo.observeCategories().first { it.size == 13 }
        }
        val seededSvcs = withTimeout(FLOW_TIMEOUT_MS) {
            catalogRepo.observeServices().first { it.size == 16 }
        }
        assertEquals(13, seededCats.size)
        assertEquals(16, seededSvcs.size)
    }

    @Test
    fun chat_adminMonitoringAndRealTimeReply_succeeds() = runBlocking {
        val customerEmail = "chatcust_${UUID.randomUUID()}@test.com"
        val customerUid = signInTestUser(customerEmail)
        val chatRepo = ChatRepository(firestore)
        val businessRepo = BusinessAndReviewsRepository(firestore)

        // 1. Customer sends message
        withTimeout(DEFAULT_TIMEOUT_MS) {
            chatRepo.sendCustomerMessage(
                customerName = "Karim Mia",
                customerUsername = "karim_mia",
                text = "Need AC servicing tomorrow"
            )
        }

        // 2. Sign in as verified bootstrapped admin
        val adminUid = signInTestUser(UserRepository.BOOTSTRAPPED_ADMIN_EMAIL)
        val url = URL("http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/projects/demo-no-project/accounts:update")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer owner")
        }
        conn.outputStream.use { out ->
            out.write("""{"localId":"$adminUid","emailVerified":true}""".toByteArray())
        }
        assertEquals(200, conn.responseCode)
        auth.signOut()
        signInTestUser(UserRepository.BOOTSTRAPPED_ADMIN_EMAIL)

        // Admin sees conversation immediately with unreadByAdmin == 1
        val allConvs = withTimeout(FLOW_TIMEOUT_MS) {
            chatRepo.observeAllConversationsByAdmin().first { list ->
                list.any { it.id == customerUid && it.unreadByAdmin == 1L }
            }
        }
        val customerConv = allConvs.first { it.id == customerUid }
        assertEquals("Karim Mia", customerConv.customerName)
        assertEquals("Need AC servicing tomorrow", customerConv.lastMessage)

        // 3. Admin opens/reads chat -> clears unreadByAdmin & sets readAt on customer's message
        withTimeout(DEFAULT_TIMEOUT_MS) {
            chatRepo.markConversationReadByAdmin(customerUid)
        }
        val afterReadConv = withTimeout(FLOW_TIMEOUT_MS) {
            chatRepo.observeConversation(customerUid).first { it != null && it.unreadByAdmin == 0L }
        }
        assertEquals(0L, afterReadConv?.unreadByAdmin)
        val msgsAfterAdminRead = withTimeout(FLOW_TIMEOUT_MS) {
            chatRepo.observeMessages(customerUid).first { list ->
                list.any { it.senderRole == "customer" && it.readAt != null }
            }
        }
        assertTrue(msgsAfterAdminRead.any { it.senderRole == "customer" && it.readAt != null })

        // 4. Admin sends reply -> creates admin message and customer notification
        withTimeout(DEFAULT_TIMEOUT_MS) {
            chatRepo.sendAdminReply(
                customerId = customerUid,
                text = "Sure, our technician will visit at 10 AM."
            )
        }

        // 5. Customer observes admin reply and notification
        signInTestUser(customerEmail)
        val custMessages = withTimeout(FLOW_TIMEOUT_MS) {
            chatRepo.observeMessages(customerUid).first { list ->
                list.any { it.senderRole == "admin" && it.text.contains("technician") }
            }
        }
        assertEquals(2, custMessages.size)

        val custNotifs = withTimeout(FLOW_TIMEOUT_MS) {
            businessRepo.observeNotifications(customerUid).first { list ->
                list.any { it.type == "message" && it.userId == customerUid }
            }
        }
        assertTrue(custNotifs.isNotEmpty())
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 15000L
        const val FLOW_TIMEOUT_MS = 10000L
    }
}

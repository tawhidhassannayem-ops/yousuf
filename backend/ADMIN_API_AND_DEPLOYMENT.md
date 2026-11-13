# Yusuf Electronics — Backend Architecture, Admin APIs & Deployment Guide

## 1. Provisioned Firebase Project & Database

- **Firebase Project ID:** `gen-lang-client-0060106260`
- **Firestore Named Database ID:** `ai-studio-android-yusufele-ed454ba4-c903-41a3-8b91-89d69af7ea53`
- **Android Application ID:** `com.aistudio.yusufelectronics.mzkqrx`

### AI Studio Environment Scope & Manual Console Steps
Within AI Studio, automated provisioning configures **Firestore Enterprise** and **Firebase Authentication (Google Sign-In provider via Jetpack Credential Manager)**.
To activate the additional production capabilities shipped in `/backend/` on your Firebase project, complete the following in the **Firebase Console** and **Firebase CLI**:

1. **Optional Email/Password Authentication (Firebase Console -> Authentication -> Sign-in method):**
   - AI Studio enforces Google Sign-In (`GetSignInWithGoogleOption`) inside the generated Android project. If you later build or customize a client outside AI Studio to also use Email/Password sign-in, enable **Email/Password** in Firebase Console.
2. **Firebase Storage (for multi-megabyte media instead of bounded JPEG Data URIs):**
   - Enable Firebase Storage in Firebase Console (`gen-lang-client-0060106260`) and deploy `/backend/storage.rules`.
3. **Firebase Cloud Messaging (FCM) Push Notifications:**
   - The Android app delivers real-time in-app and Android local notifications whenever Firestore `notifications/{id}` records arrive.
   - To deliver background push notifications when the app is closed, enable **Cloud Messaging** in Firebase Console and deploy `/backend/functions/index.js` (`onBookingStatusUpdated`, `onChatMessageCreated`).
4. **Cloud Functions (Blaze Plan Required):**
   - Deploy `/backend/functions/` after upgrading project `gen-lang-client-0060106260` to Blaze if you want server-side triggers and HTTPS callable admin APIs.

---

## 2. Firestore Data Model (Shared by Customer App & Future Admin Web/Mobile App)

| Collection | Key Fields | Access Control |
|---|---|---|
| `users/{uid}` | `userId`, `fullName`, `username`, `email`, `phone`, `address`, `profileImage`, `preferredLanguage`, `notificationsEnabled`, `accountStatus`, `role`, `createdAt`, `updatedAt` | Owner RW (role/status immutable), Admin RW |
| `usernames/{username}` | `userId`, `createdAt` | Signed-in `get` (uniqueness check), owner create/delete |
| `admins/{uid}` | `adminId`, `name`, `email`, `role`, `status`, `createdAt` | Owner/Admin `get`, Super-admin write |
| `service_categories/{id}` | `nameEn`, `nameBn`, `descriptionEn`, `descriptionBn`, `iconKey`, `status`, `sortOrder`, `createdAt`, `updatedAt` | Signed-in read, Admin write |
| `services/{id}` | `categoryId`, `nameEn`, `nameBn`, `descriptionEn`, `descriptionBn`, `featuresEn`, `featuresBn`, `price`, `priceType`, `priceDisplayEn`, `priceDisplayBn`, `availability`, `isPopular`, `createdAt`, `updatedAt` | Signed-in read, Admin write |
| `bookings/{bookingId}` | `bookingCode`, `customerId`, `serviceId`, `categoryId`, `customerName`, `phone`, `address`, `cityArea`, `description`, `imageUrls`, `preferredDate`, `preferredTime`, `bookingStatus`, `adminNotes`, `hasReview`, `createdAt`, `updatedAt` | Owner create/read/cancel, Admin read/update/delete |
| `conversations/{customerId}` | `customerId`, `customerName`, `customerUsername`, `lastMessage`, `unreadByAdmin`, `unreadByCustomer`, `status`, `lastMessageAt`, `createdAt` | Owner/Admin RW |
| `conversations/{customerId}/messages/{msgId}` | `conversationId`, `customerId`, `senderId`, `senderRole`, `messageType`, `text`, `imageUrl`, `sentAt`, `readAt` | Owner/Admin read & role-validated create |
| `notifications/{notifId}` | `userId` (`uid` or `"all"`), `titleEn`, `titleBn`, `bodyEn`, `bodyBn`, `type`, `relatedId`, `isRead`, `createdAt` | Target user or `"all"` read; Owner mark `isRead`; Admin write |
| `reviews/{reviewId}` | `bookingId`, `serviceId`, `customerId`, `customerName`, `rating`, `comment`, `approvalStatus`, `createdAt` | Approved reviews public to signed-in users; Customer creates for own completed booking; Admin moderates |
| `business_settings/main` | `businessName`, `phoneNumber`, `whatsappNumber`, `email`, `addressEn`, `addressBn`, `mapsQueryUrl`, `workingHoursEn`, `workingHoursBn`, `availabilityStatus`, `serviceAreasEn`, `serviceAreasBn`, `updatedAt` | Signed-in read, Admin write |
| `audit_logs/{logId}` | `actorId`, `action`, `targetCollection`, `targetId`, `details`, `createdAt` | Admin read/create |

---

## 3. Deploying Backend Rules, Indexes, Functions & Seeding Data

```bash
# 1. Deploy Firestore rules & composite indexes
firebase deploy --project gen-lang-client-0060106260 --only firestore:rules
firebase deploy --project gen-lang-client-0060106260 --only firestore:indexes

# 2. Deploy Storage rules
firebase deploy --project gen-lang-client-0060106260 --only storage

# 3. Install & deploy Cloud Functions
cd backend/functions && npm install && cd ../..
firebase deploy --project gen-lang-client-0060106260 --only functions

# 4. Seed the 13 default categories, services & business_settings/main
export GOOGLE_APPLICATION_CREDENTIALS=./service-account.json
node backend/seed.js
```

Note: The in-app **Admin Control Console** (visible in the Profile screen when signed in as an authorized admin) also automatically seeds the 13 default categories/services and allows updating live `phoneNumber`, `whatsappNumber`, `email`, `address`, and `ONLINE`/`BUSY`/`OFFLINE` availability status without needing CLI access.

---

## 4. Admin-Ready APIs for a Future Admin Web / Mobile App

Any future Admin React/Next.js Web App or Flutter/Android Admin App connecting to project `gen-lang-client-0060106260` with database ID `ai-studio-android-yusufele-ed454ba4-c903-41a3-8b91-89d69af7ea53` can immediately:

1. **Authenticate Admin:** Sign in with an authorized admin account (`admins/{uid}` with `status == "active"`, custom claim `admin: true`, or verified bootstrapped owner email) — zero rebuild of Firestore rules required.
2. **Real-Time Listeners:** Listen directly to `bookings`, `conversations`, `service_categories`, `services`, `users`, `reviews`, and `business_settings/main`.
3. **Callable Cloud Functions (`/backend/functions/index.js`):**
   - `adminUpdateBookingStatus({ bookingId, bookingStatus, adminNotes })`
   - `adminSetRole({ targetUid, name, email, role, active })`
   - `adminGetDashboardMetrics()`

---

## 5. Building Android APK & Play Store App Bundle (AAB)

```bash
# Debug APK (for direct device testing)
gradle assembleDebug

# Signed Release APK
gradle assembleRelease

# Signed Android App Bundle (AAB for Google Play Console)
gradle bundleRelease
```

### Google Play Data Safety Declaration Summary
- **Data Collected:** Name, Email address, Optional Phone number, Service Address, In-app Messages, Service Booking history, Optional Problem Photos (via Android Photo Picker — zero broad storage permissions required).
- **Data Sharing:** None with third-party advertisers.
- **Deletion:** Users can permanently delete their profile and username mapping directly inside **Profile -> Delete Account**.

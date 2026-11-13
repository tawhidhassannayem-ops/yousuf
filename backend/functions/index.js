const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { onCall, HttpsError } = require("firebase-functions/v2/https");

initializeApp();

// Matches res/values/firebase_applet_config.xml -> firestore_database_id
const DATABASE_ID =
  process.env.FIRESTORE_DATABASE_ID ||
  "ai-studio-android-yusufele-ed454ba4-c903-41a3-8b91-89d69af7ea53";

const db = getFirestore(DATABASE_ID);

async function assertAdmin(authContext) {
  if (!authContext || !authContext.uid) {
    throw new HttpsError("unauthenticated", "Authentication required.");
  }
  if (authContext.token?.admin === true) {
    return authContext.uid;
  }
  const adminSnap = await db.collection("admins").doc(authContext.uid).get();
  if (adminSnap.exists && adminSnap.data()?.status === "active") {
    return authContext.uid;
  }
  throw new HttpsError("permission-denied", "Admin privileges required.");
}

async function sendFcmToUser(userId, title, body, dataPayload = {}) {
  const userDoc = await db.collection("users").doc(userId).get();
  if (!userDoc.exists) return;
  const userData = userDoc.data() || {};
  if (userData.notificationsEnabled === false) return;
  const token = userData.fcmToken;
  if (!token) return;

  try {
    await getMessaging().send({
      token,
      notification: { title, body },
      data: dataPayload,
      android: { priority: "high" },
    });
  } catch (err) {
    console.warn(`FCM delivery failed for user ${userId}:`, err.message);
  }
}

/**
 * Trigger: New booking created -> audit log
 */
exports.onBookingCreated = onDocumentCreated(
  {
    document: "bookings/{bookingId}",
    database: DATABASE_ID,
  },
  async (event) => {
    const snap = event.data;
    if (!snap) return;
    const booking = snap.data();
    const bookingId = event.params.bookingId;

    await db.collection("audit_logs").add({
      actorId: booking.customerId,
      action: "BOOKING_CREATED",
      targetCollection: "bookings",
      targetId: bookingId,
      details: `${booking.bookingCode} (${booking.serviceNameEn || booking.serviceId})`,
      createdAt: FieldValue.serverTimestamp(),
    });
  }
);

/**
 * Trigger: Booking status updated by admin -> FCM push notification + audit log
 */
exports.onBookingStatusUpdated = onDocumentUpdated(
  {
    document: "bookings/{bookingId}",
    database: DATABASE_ID,
  },
  async (event) => {
    const before = event.data.before.data();
    const after = event.data.after.data();
    if (!before || !after || before.bookingStatus === after.bookingStatus) return;

    const bookingId = event.params.bookingId;
    await db.collection("audit_logs").add({
      actorId: "admin_trigger",
      action: "BOOKING_STATUS_CHANGED",
      targetCollection: "bookings",
      targetId: bookingId,
      details: `${before.bookingStatus} -> ${after.bookingStatus}`,
      createdAt: FieldValue.serverTimestamp(),
    });

    const title = `Booking ${after.bookingCode}: ${String(after.bookingStatus).toUpperCase()}`;
    const body = `Your ${after.serviceNameEn || "service"} booking is now ${after.bookingStatus}.`;
    await sendFcmToUser(after.customerId, title, body, {
      type: "booking",
      bookingId,
      bookingStatus: String(after.bookingStatus),
    });
  }
);

/**
 * Trigger: New message in customer-admin conversation -> push alert to recipient
 */
exports.onChatMessageCreated = onDocumentCreated(
  {
    document: "conversations/{conversationId}/messages/{messageId}",
    database: DATABASE_ID,
  },
  async (event) => {
    const snap = event.data;
    if (!snap) return;
    const msg = snap.data();
    const conversationId = event.params.conversationId;

    if (msg.senderRole === "admin") {
      const preview = msg.text || "Sent an image attachment";
      await sendFcmToUser(conversationId, "Yusuf Electronics Support", preview, {
        type: "message",
        conversationId,
      });
    }
  }
);

/**
 * Admin-Ready HTTPS Callable: Update booking status from future Admin Web / Mobile App
 */
exports.adminUpdateBookingStatus = onCall(async (request) => {
  const adminUid = await assertAdmin(request.auth);
  const { bookingId, bookingStatus, adminNotes = "" } = request.data || {};
  const allowedStatuses = new Set([
    "pending",
    "accepted",
    "rejected",
    "scheduled",
    "in_progress",
    "completed",
    "cancelled",
  ]);
  if (!bookingId || !allowedStatuses.has(bookingStatus)) {
    throw new HttpsError("invalid-argument", "Invalid bookingId or bookingStatus.");
  }

  const ref = db.collection("bookings").doc(bookingId);
  const snap = await ref.get();
  if (!snap.exists) {
    throw new HttpsError("not-found", "Booking not found.");
  }
  const booking = snap.data();

  const batch = db.batch();
  batch.update(ref, {
    bookingStatus,
    adminNotes: String(adminNotes),
    updatedAt: FieldValue.serverTimestamp(),
  });
  batch.set(db.collection("notifications").doc(), {
    userId: booking.customerId,
    titleEn: `Booking ${booking.bookingCode}: ${bookingStatus}`,
    titleBn: `বুকিং ${booking.bookingCode}: ${bookingStatus}`,
    bodyEn: `Your booking status is now ${bookingStatus}.`,
    bodyBn: `আপনার বুকিংয়ের বর্তমান অবস্থা: ${bookingStatus}।`,
    type: "booking",
    relatedId: bookingId,
    isRead: false,
    createdAt: FieldValue.serverTimestamp(),
  });
  batch.set(db.collection("audit_logs").doc(), {
    actorId: adminUid,
    action: "UPDATE_BOOKING_STATUS",
    targetCollection: "bookings",
    targetId: bookingId,
    details: `${booking.bookingCode} -> ${bookingStatus}`,
    createdAt: FieldValue.serverTimestamp(),
  });
  await batch.commit();

  return { ok: true, bookingId, bookingStatus };
});

/**
 * Admin-Ready HTTPS Callable: Grant or revoke admin privileges (sets Firestore + Custom Claim)
 */
exports.adminSetRole = onCall(async (request) => {
  const actorUid = await assertAdmin(request.auth);
  const { targetUid, name = "Admin", email = "", role = "admin", active = true } = request.data || {};
  if (!targetUid) {
    throw new HttpsError("invalid-argument", "targetUid is required.");
  }

  const status = active ? "active" : "inactive";
  await getAuth().setCustomUserClaims(targetUid, { admin: Boolean(active), role });
  await db.collection("admins").doc(targetUid).set(
    {
      adminId: targetUid,
      name,
      email,
      role: role === "super_admin" ? "super_admin" : "admin",
      status,
      createdAt: FieldValue.serverTimestamp(),
    },
    { merge: true }
  );

  await db.collection("audit_logs").add({
    actorId: actorUid,
    action: "ADMIN_ROLE_UPDATED",
    targetCollection: "admins",
    targetId: targetUid,
    details: `role=${role}, status=${status}`,
    createdAt: FieldValue.serverTimestamp(),
  });

  return { ok: true, targetUid, role, status };
});

/**
 * Admin-Ready HTTPS Callable: Dashboard analytics for future Admin Web / Mobile App
 */
exports.adminGetDashboardMetrics = onCall(async (request) => {
  await assertAdmin(request.auth);

  const [customersSnap, bookingsSnap, conversationsSnap, servicesSnap] = await Promise.all([
    db.collection("users").count().get(),
    db.collection("bookings").get(),
    db.collection("conversations").where("unreadByAdmin", ">", 0).count().get(),
    db.collection("services").where("availability", "==", "available").count().get(),
  ]);

  const byStatus = {};
  bookingsSnap.forEach((doc) => {
    const st = doc.data().bookingStatus || "pending";
    byStatus[st] = (byStatus[st] || 0) + 1;
  });

  return {
    totalCustomers: customersSnap.data().count,
    totalBookings: bookingsSnap.size,
    bookingsByStatus: byStatus,
    unreadConversations: conversationsSnap.data().count,
    activeServices: servicesSnap.data().count,
  };
});

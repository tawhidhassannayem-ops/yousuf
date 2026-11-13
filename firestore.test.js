const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const ts = () => new Date(Date.now() - 1000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_EMAIL = "tawhidhassannayem@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function adminContext() {
  return testEnv.authenticatedContext("super_admin_uid", {
    email: ADMIN_EMAIL,
    email_verified: true,
  });
}

function spoofedAdminContext() {
  return testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  });
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("service_categories").get());
  await assertFails(unauthDb.collection("services").get());
  await assertFails(unauthDb.collection("bookings").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("conversations").doc(ALICE_UID).get());
});

test("Customer cannot self-assign admin role or read another customer's PII", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Self-assigning role='admin' must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Rahman",
      username: "alice_r",
      email: "alice@example.com",
      role: "admin",
      accountStatus: "active",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Valid customer creation succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Rahman",
      username: "alice_r",
      email: "alice@example.com",
      role: "customer",
      accountStatus: "active",
      preferredLanguage: "bn",
      notificationsEnabled: true,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Ghost field shadow update fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isVerified: true,
      updatedAt: ts(),
    })
  );

  // Bob cannot read Alice's user doc
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  // Bob cannot list users
  await assertFails(bobDb.collection("users").get());
});

test("Spoofed admin email with email_verified=false is rejected", async () => {
  const spoofDb = spoofedAdminContext().firestore();
  await assertFails(
    spoofDb.collection("service_categories").doc("fan_repair").set({
      nameEn: "Fan Repair",
      nameBn: "ফ্যান মেরামত",
      descriptionEn: "Ceiling and table fan repair",
      descriptionBn: "সিলিং ও টেবিল ফ্যান মেরামত",
      status: "active",
      sortOrder: 1,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );
});

test("Verified admin can manage categories, services, and business settings; customers can only read", async () => {
  const admDb = adminContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertSucceeds(
    admDb.collection("service_categories").doc("fan_repair").set({
      nameEn: "Fan Repair & Installation",
      nameBn: "ফ্যান মেরামত ও ইনস্টলেশন",
      descriptionEn: "All fan services",
      descriptionBn: "সব ধরনের ফ্যান সার্ভিস",
      iconKey: "fan",
      status: "active",
      sortOrder: 1,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  await assertSucceeds(aliceDb.collection("service_categories").get());
  await assertFails(
    aliceDb.collection("service_categories").doc("hack").set({
      nameEn: "Hack",
      nameBn: "হ্যাক",
      descriptionEn: "No",
      descriptionBn: "না",
      status: "active",
      sortOrder: 2,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );
});

test("Bookings enforce pending initial status, owner isolation, and admin status transitions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const admDb = adminContext().firestore();

  const baseBooking = {
    bookingCode: "YE-1001",
    customerId: ALICE_UID,
    serviceId: "svc_fan_cap",
    categoryId: "fan_repair",
    serviceNameEn: "Capacitor Replacement",
    serviceNameBn: "ক্যাপাসিটর পরিবর্তন",
    customerName: "Alice Rahman",
    phone: "01700000000",
    address: "House 12, Road 4",
    cityArea: "Mirpur",
    description: "Ceiling fan running slow",
    preferredDate: "2026-11-15",
    preferredTime: "10:00 AM - 12:00 PM",
    bookingStatus: "accepted",
    hasReview: false,
    createdAt: ts(),
    updatedAt: ts(),
  };

  // Auto-confirming on create must fail
  await assertFails(aliceDb.collection("bookings").doc("b_1").set(baseBooking));

  // Pending status succeeds
  await assertSucceeds(
    aliceDb.collection("bookings").doc("b_1").set({
      ...baseBooking,
      bookingStatus: "pending",
    })
  );

  // Scoped list query succeeds for Alice, unfiltered list fails for Alice
  await assertSucceeds(aliceDb.collection("bookings").where("customerId", "==", ALICE_UID).get());
  await assertFails(aliceDb.collection("bookings").get());

  // Bob cannot read Alice's booking
  await assertFails(bobDb.collection("bookings").doc("b_1").get());

  // Admin can update booking to completed
  await assertSucceeds(
    admDb.collection("bookings").doc("b_1").update({
      bookingStatus: "completed",
      adminNotes: "Technician replaced capacitor",
      updatedAt: ts(),
    })
  );
});

test("Reviews require an existing completed booking belonging to the customer", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const admDb = adminContext().firestore();

  await assertSucceeds(
    aliceDb.collection("bookings").doc("b_rev").set({
      bookingCode: "YE-1002",
      customerId: ALICE_UID,
      serviceId: "svc_ac",
      categoryId: "ac_services",
      customerName: "Alice Rahman",
      phone: "01700000000",
      address: "House 12",
      cityArea: "Dhanmondi",
      description: "AC cooling issue",
      preferredDate: "2026-11-16",
      preferredTime: "02:00 PM",
      bookingStatus: "pending",
      hasReview: false,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Review while pending must fail
  await assertFails(
    aliceDb.collection("reviews").doc("rev_1").set({
      customerId: ALICE_UID,
      customerName: "Alice Rahman",
      bookingId: "b_rev",
      serviceId: "svc_ac",
      rating: 5,
      comment: "Great work",
      approvalStatus: "approved",
      createdAt: ts(),
    })
  );

  // Admin completes booking
  await assertSucceeds(
    admDb.collection("bookings").doc("b_rev").update({
      bookingStatus: "completed",
      updatedAt: ts(),
    })
  );

  // Bob still cannot review Alice's booking
  await assertFails(
    bobDb.collection("reviews").doc("rev_bob").set({
      customerId: BOB_UID,
      customerName: "Bob",
      bookingId: "b_rev",
      serviceId: "svc_ac",
      rating: 5,
      comment: "Fake review",
      approvalStatus: "approved",
      createdAt: ts(),
    })
  );

  // Alice can review completed booking
  await assertSucceeds(
    aliceDb.collection("reviews").doc("rev_1").set({
      customerId: ALICE_UID,
      customerName: "Alice Rahman",
      bookingId: "b_rev",
      serviceId: "svc_ac",
      rating: 5,
      comment: "Fast and professional AC servicing!",
      approvalStatus: "approved",
      createdAt: ts(),
    })
  );
});

test("Real-time conversations and messages are isolated per customer and accessible by admin", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const admDb = adminContext().firestore();

  await assertSucceeds(
    aliceDb.collection("conversations").doc(ALICE_UID).set({
      customerId: ALICE_UID,
      customerName: "Alice Rahman",
      lastMessage: "Need an electrician",
      lastMessageAt: ts(),
      unreadByCustomer: 0,
      unreadByAdmin: 1,
      status: "open",
      createdAt: ts(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("conversations").doc(ALICE_UID).collection("messages").doc("msg_1").set({
      conversationId: ALICE_UID,
      customerId: ALICE_UID,
      senderId: ALICE_UID,
      senderRole: "customer",
      messageType: "text",
      text: "Need an electrician",
      sentAt: ts(),
    })
  );

  // Customer cannot spoof senderRole='admin'
  await assertFails(
    aliceDb.collection("conversations").doc(ALICE_UID).collection("messages").doc("msg_spoof").set({
      conversationId: ALICE_UID,
      customerId: ALICE_UID,
      senderId: ALICE_UID,
      senderRole: "admin",
      messageType: "text",
      text: "Free discount",
      sentAt: ts(),
    })
  );

  // Bob cannot read Alice's messages
  await assertFails(
    bobDb.collection("conversations").doc(ALICE_UID).collection("messages").get()
  );

  // Admin can reply
  await assertSucceeds(
    admDb.collection("conversations").doc(ALICE_UID).collection("messages").doc("msg_2").set({
      conversationId: ALICE_UID,
      customerId: ALICE_UID,
      senderId: "super_admin_uid",
      senderRole: "admin",
      messageType: "text",
      text: "Hello Alice, we are on the way!",
      sentAt: ts(),
    })
  );
});

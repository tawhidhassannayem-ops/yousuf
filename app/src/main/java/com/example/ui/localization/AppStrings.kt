package com.example.ui.localization

import androidx.compose.runtime.compositionLocalOf
import com.example.data.model.BookingStatus
import com.example.data.model.BusinessAvailability

data class AppStrings(
    val isBangla: Boolean,
    val brandName: String,
    val tagline: String,
    val languageLabel: String,
    val switchLangAction: String,

    // Auth & Registration
    val welcomeTitle: String,
    val authSubtitle: String,
    val continueWithGoogle: String,
    val authSecurityNote: String,
    val completeProfileTitle: String,
    val completeProfileSubtitle: String,
    val fullNameLabel: String,
    val usernameLabel: String,
    val usernameHint: String,
    val emailLabel: String,
    val phoneOptionalLabel: String,
    val addressLabel: String,
    val saveAndContinue: String,
    val validationNameRequired: String,
    val validationUsernameInvalid: String,
    val validationUsernameTaken: String,

    // Navigation
    val navHome: String,
    val navServices: String,
    val navMessages: String,
    val navBookings: String,
    val navProfile: String,

    // Business Status
    val statusOnline: String,
    val statusBusy: String,
    val statusOffline: String,
    val statusOnlineDesc: String,
    val statusBusyOrOfflineBanner: String,

    // Home Screen
    val welcomePrefix: String,
    val searchPlaceholder: String,
    val promoTitle: String,
    val promoSubtitle: String,
    val promoCta: String,
    val popularServices: String,
    val allServices: String,
    val seeAll: String,
    val emergencyContact: String,
    val contactYusufBtn: String,
    val recentBookings: String,
    val customerReviews: String,
    val noBookingsYet: String,
    val noReviewsYet: String,
    val catalogEmptyMessage: String,

    // Services & Details
    val categoriesTitle: String,
    val allCategoriesChip: String,
    val availableBadge: String,
    val unavailableBadge: String,
    val bookServiceBtn: String,
    val contactBtn: String,
    val serviceDetailsTitle: String,
    val featuresTitle: String,
    val estimatedPriceTitle: String,
    val sendMessageBtn: String,
    val callNowBtn: String,
    val noSearchResults: String,

    // Booking
    val bookServiceTitle: String,
    val customerNameField: String,
    val phoneNumberField: String,
    val serviceCategoryField: String,
    val selectedServiceField: String,
    val completeAddressField: String,
    val cityAreaField: String,
    val problemDescField: String,
    val problemImagesLabel: String,
    val addPhotoBtn: String,
    val preferredDateField: String,
    val preferredTimeField: String,
    val submitBookingBtn: String,
    val bookingSubmittedTitle: String,
    val bookingSubmittedMsg: String,
    val myBookingsTitle: String,
    val cancelBookingBtn: String,
    val writeReviewBtn: String,
    val reviewedBadge: String,
    val adminNotesLabel: String,
    val validationBookingFields: String,

    // Chat
    val messagesTitle: String,
    val typeMessageHint: String,
    val sendBtn: String,
    val attachPhotoDesc: String,
    val chatEmptyHint: String,
    val readLabel: String,
    val sentLabel: String,

    // Notifications
    val notificationsTitle: String,
    val noNotificationsYet: String,

    // Contact Us
    val contactUsTitle: String,
    val phoneLabel: String,
    val whatsappLabel: String,
    val addressContactLabel: String,
    val workingHoursLabel: String,
    val serviceAreasLabel: String,
    val notConfiguredYet: String,
    val whatsappBtn: String,
    val viewLocationBtn: String,

    // Reviews
    val rateServiceTitle: String,
    val ratingPrompt: String,
    val reviewCommentHint: String,
    val submitReviewBtn: String,

    // Profile & Legal
    val profileTitle: String,
    val editProfileBtn: String,
    val accountCreatedLabel: String,
    val notificationSettingsLabel: String,
    val privacyPolicyTitle: String,
    val termsTitle: String,
    val logoutBtn: String,
    val deleteAccountBtn: String,
    val deleteAccountConfirmTitle: String,
    val deleteAccountConfirmBody: String,
    val confirmBtn: String,
    val cancelBtn: String,
    val retryBtn: String,
    val privacyPolicyBody: String,
    val termsBody: String,

    // Admin Console
    val adminConsoleTitle: String,
    val adminBadge: String
) {
    fun bookingStatusText(status: BookingStatus): String = if (isBangla) {
        when (status) {
            BookingStatus.PENDING -> "অপেক্ষমাণ"
            BookingStatus.ACCEPTED -> "গৃহীত"
            BookingStatus.REJECTED -> "প্রত্যাখ্যাত"
            BookingStatus.SCHEDULED -> "নির্ধারিত"
            BookingStatus.IN_PROGRESS -> "চলমান"
            BookingStatus.COMPLETED -> "সম্পন্ন"
            BookingStatus.CANCELLED -> "বাতিল"
        }
    } else {
        when (status) {
            BookingStatus.PENDING -> "Pending"
            BookingStatus.ACCEPTED -> "Accepted"
            BookingStatus.REJECTED -> "Rejected"
            BookingStatus.SCHEDULED -> "Scheduled"
            BookingStatus.IN_PROGRESS -> "In Progress"
            BookingStatus.COMPLETED -> "Completed"
            BookingStatus.CANCELLED -> "Cancelled"
        }
    }

    fun businessStatusLabel(status: BusinessAvailability): String = if (isBangla) {
        when (status) {
            BusinessAvailability.ONLINE -> statusOnline
            BusinessAvailability.BUSY -> statusBusy
            BusinessAvailability.OFFLINE -> statusOffline
        }
    } else {
        when (status) {
            BusinessAvailability.ONLINE -> statusOnline
            BusinessAvailability.BUSY -> statusBusy
            BusinessAvailability.OFFLINE -> statusOffline
        }
    }
}

val BanglaStrings = AppStrings(
    isBangla = true,
    brandName = "YUSUF ELECTRONICS",
    tagline = "বিশ্বস্ত ইলেকট্রিক্যাল, এসি, সিসিটিভি ও হোম সার্ভিস",
    languageLabel = "বাংলা",
    switchLangAction = "English",

    welcomeTitle = "ইউসুফ ইলেকট্রনিক্সে স্বাগতম",
    authSubtitle = "নিরাপদ সেবা বুকিং এবং সরাসরি সাপোর্টের জন্য সাইন ইন করুন।",
    continueWithGoogle = "অ্যাকাউন্ট দিয়ে সাইন ইন করুন",
    authSecurityNote = "আপনার অ্যাকাউন্ট ও তথ্য ক্লাউড ফায়ারবেসে সুরক্ষিত থাকে।",
    completeProfileTitle = "আপনার প্রোফাইল সম্পূর্ণ করুন",
    completeProfileSubtitle = "সার্ভিস বুকিং ও চ্যাটের জন্য আপনার নাম ও ইউজারনেম সেট করুন।",
    fullNameLabel = "পূর্ণ নাম",
    usernameLabel = "ইউজারনেম (ইংরেজি অক্ষর/সংখ্যা)",
    usernameHint = "যেমন: rahim_electronics",
    emailLabel = "ইমেইল ঠিকানা",
    phoneOptionalLabel = "ফোন নম্বর (ঐচ্ছিক)",
    addressLabel = "ঠিকানা",
    saveAndContinue = "সংরক্ষণ করে এগিয়ে যান",
    validationNameRequired = "অনুগ্রহ করে আপনার পূর্ণ নাম লিখুন।",
    validationUsernameInvalid = "ইউজারনেম কমপক্ষে ৩ অক্ষরের (a-z, 0-9, _) হতে হবে।",
    validationUsernameTaken = "এই ইউজারনেমটি ইতিমধ্যেই ব্যবহৃত হয়েছে। অন্য একটি বেছে নিন।",

    navHome = "হোম",
    navServices = "সার্ভিস",
    navMessages = "মেসেজ",
    navBookings = "বুকিং",
    navProfile = "প্রোফাইল",

    statusOnline = "অনলাইন",
    statusBusy = "ব্যস্ত",
    statusOffline = "অফলাইন",
    statusOnlineDesc = "ইউসুফ ইলেকট্রনিক্স এখন সাপোর্ট ও বুকিংয়ের জন্য উপলব্ধ।",
    statusBusyOrOfflineBanner = "ইউসুফ ইলেকট্রনিক্সে যোগাযোগ করার জন্য ধন্যবাদ। আমরা বর্তমানে ব্যস্ত বা অনুপলব্ধ। অনুগ্রহ করে আপনার বার্তা রেখে যান, আমরা যত দ্রুত সম্ভব উত্তর দেব।",

    welcomePrefix = "স্বাগতম,",
    searchPlaceholder = "ফ্যান, এসি, সিসিটিভি বা প্লাম্বিং খুঁজুন...",
    promoTitle = "দক্ষ টেকনিশিয়ান দ্বারা দ্রুত হোম সার্ভিস",
    promoSubtitle = "বাসা ও অফিসের সব ধরনের ইলেকট্রিক্যাল, এসি, আইপিএস ও সিসিটিভি সমাধান এক অ্যাপেই।",
    promoCta = "সার্ভিস বুক করুন",
    popularServices = "জনপ্রিয় সার্ভিস",
    allServices = "সকল সার্ভিস ক্যাটাগরি",
    seeAll = "সব দেখুন",
    emergencyContact = "জরুরি যোগাযোগ",
    contactYusufBtn = "ইউসুফ ইলেকট্রনিক্সের সাথে যোগাযোগ করুন",
    recentBookings = "সাম্প্রতিক বুকিং",
    customerReviews = "গ্রাহকদের রিভিউ",
    noBookingsYet = "এখনো কোনো বুকিং নেই। প্রয়োজন অনুযায়ী সার্ভিস বুক করুন।",
    noReviewsYet = "এখনো কোনো রিভিউ প্রকাশিত হয়নি।",
    catalogEmptyMessage = "সার্ভিস তালিকা লোড হচ্ছে বা অ্যাডমিন কর্তৃক আপডেট করা হচ্ছে। সরাসরি মেসেজে যোগাযোগ করুন।",

    categoriesTitle = "সকল সার্ভিস",
    allCategoriesChip = "সবগুলো",
    availableBadge = "সার্ভিস চালু আছে",
    unavailableBadge = "সাময়িক বন্ধ",
    bookServiceBtn = "বুক করুন",
    contactBtn = "যোগাযোগ",
    serviceDetailsTitle = "সার্ভিসের বিস্তারিত",
    featuresTitle = "সার্ভিসে যা থাকছে",
    estimatedPriceTitle = "আনুমানিক চার্জ",
    sendMessageBtn = "মেসেজ পাঠান",
    callNowBtn = "এখনই কল করুন",
    noSearchResults = "আপনার খোঁজা সার্ভিসটি পাওয়া যায়নি। অন্য কীওয়ার্ড দিয়ে চেষ্টা করুন বা সরাসরি মেসেজ করুন।",

    bookServiceTitle = "সার্ভিস বুকিং ফর্ম",
    customerNameField = "গ্রাহকের নাম",
    phoneNumberField = "ফোন নম্বর",
    serviceCategoryField = "সার্ভিস ক্যাটাগরি",
    selectedServiceField = "নির্বাচিত সার্ভিস",
    completeAddressField = "সম্পূর্ণ ঠিকানা (বাসা/রোড)",
    cityAreaField = "শহর / এলাকা",
    problemDescField = "সমস্যার বিস্তারিত বিবরণ",
    problemImagesLabel = "সমস্যার ছবি (ঐচ্ছিক, সর্বোচ্চ ৩টি)",
    addPhotoBtn = "ছবি যোগ করুন",
    preferredDateField = "পছন্দের তারিখ",
    preferredTimeField = "পছন্দের সময়",
    submitBookingBtn = "বুকিং জমা দিন",
    bookingSubmittedTitle = "বুকিং অনুরোধ সফল!",
    bookingSubmittedMsg = "আপনার বুকিং আইডি তৈরি হয়েছে। অ্যাডমিন যাচাই করে নিশ্চিত করবেন।",
    myBookingsTitle = "আমার বুকিংসমূহ",
    cancelBookingBtn = "বাতিল করুন",
    writeReviewBtn = "রিভিউ দিন",
    reviewedBadge = "রিভিউ সম্পন্ন",
    adminNotesLabel = "অ্যাডমিনের নোট",
    validationBookingFields = "অনুগ্রহ করে নাম, ফোন, ঠিকানা, এলাকা, সমস্যার বিবরণ, তারিখ ও সময় সঠিকভাবে পূরণ করুন।",

    messagesTitle = "সরাসরি চ্যাট",
    typeMessageHint = "আপনার বার্তা লিখুন...",
    sendBtn = "পাঠান",
    attachPhotoDesc = "ছবি সংযুক্ত করুন",
    chatEmptyHint = "যেকোনো ইলেকট্রিক্যাল সমস্যার জন্য আমাদের মেসেজ বা ছবি পাঠান।",
    readLabel = "পঠিত",
    sentLabel = "প্রেরিত",

    notificationsTitle = "নোটিফিকেশন",
    noNotificationsYet = "এখনো কোনো নোটিফিকেশন নেই।",

    contactUsTitle = "যোগাযোগ ও তথ্য",
    phoneLabel = "ফোন নম্বর",
    whatsappLabel = "হোয়াটসঅ্যাপ",
    addressContactLabel = "ব্যবসার ঠিকানা",
    workingHoursLabel = "কর্মঘণ্টা",
    serviceAreasLabel = "সার্ভিস এলাকা",
    notConfiguredYet = "অ্যাডমিন কর্তৃক এখনো সেট করা হয়নি",
    whatsappBtn = "হোয়াটসঅ্যাপ",
    viewLocationBtn = "লোকেশন দেখুন",

    rateServiceTitle = "সার্ভিস রেটিং ও রিভিউ",
    ratingPrompt = "সার্ভিসটি কেমন ছিল? (১–৫ স্টার)",
    reviewCommentHint = "আপনার অভিজ্ঞতা লিখুন...",
    submitReviewBtn = "রিভিউ জমা দিন",

    profileTitle = "আমার প্রোফাইল",
    editProfileBtn = "প্রোফাইল সম্পাদনা",
    accountCreatedLabel = "অ্যাকাউন্ট তৈরির তারিখ",
    notificationSettingsLabel = "নোটিফিকেশন চালু রাখুন",
    privacyPolicyTitle = "গোপনীয়তা নীতি (Privacy Policy)",
    termsTitle = "শর্তাবলী (Terms & Conditions)",
    logoutBtn = "লগ আউট",
    deleteAccountBtn = "অ্যাকাউন্ট মুছে ফেলুন",
    deleteAccountConfirmTitle = "অ্যাকাউন্ট মুছে ফেলতে চান?",
    deleteAccountConfirmBody = "এটি আপনার প্রোফাইল এবং ইউজারনেম স্থায়ীভাবে মুছে ফেলবে। আপনি কি নিশ্চিত?",
    confirmBtn = "নিশ্চিত করুন",
    cancelBtn = "বাতিল",
    retryBtn = "আবার চেষ্টা করুন",
    privacyPolicyBody = "ইউসুফ ইলেকট্রনিক্স শুধুমাত্র সার্ভিস বুকিং, টেকনিশিয়ান যোগাযোগ এবং গ্রাহক সাপোর্টের জন্য আপনার নাম, ইমেইল, ফোন নম্বর ও ঠিকানা সংরক্ষণ করে। আমরা কোনো তৃতীয় পক্ষের কাছে আপনার ব্যক্তিগত তথ্য বিক্রি বা প্রকাশ করি না। আপনি প্রোফাইল থেকে যেকোনো সময় আপনার অ্যাকাউন্ট মুছে ফেলতে পারবেন।",
    termsBody = "সার্ভিস বুকিং জমা দেওয়ার পর অ্যাডমিনের অনুমোদনের মাধ্যমে সময় নিশ্চিত করা হয়। সার্ভিসের চার্জ কাজের ধরন ও পরিদর্শনের ওপর ভিত্তি করে নির্ধারিত হতে পারে। গ্রাহক কাজ শুরু হওয়ার আগে যেকোনো বুকিং বাতিল করতে পারবেন।",

    adminConsoleTitle = "অ্যাডমিন কন্ট্রোল প্যানেল",
    adminBadge = "অ্যাডমিন"
)

val EnglishStrings = AppStrings(
    isBangla = false,
    brandName = "YUSUF ELECTRONICS",
    tagline = "Trusted Electrical, AC, CCTV & Home Services",
    languageLabel = "English",
    switchLangAction = "বাংলা",

    welcomeTitle = "Welcome to Yusuf Electronics",
    authSubtitle = "Sign in to book electrical services and chat directly with our team.",
    continueWithGoogle = "Sign in to Continue",
    authSecurityNote = "Your account and service records are protected in Cloud Firestore.",
    completeProfileTitle = "Complete Your Profile",
    completeProfileSubtitle = "Set your full name and unique username for bookings and support chat.",
    fullNameLabel = "Full Name",
    usernameLabel = "Username (letters/numbers/_)",
    usernameHint = "e.g. rahim_electronics",
    emailLabel = "Email Address",
    phoneOptionalLabel = "Phone Number (Optional)",
    addressLabel = "Address",
    saveAndContinue = "Save & Continue",
    validationNameRequired = "Please enter your full name.",
    validationUsernameInvalid = "Username must be at least 3 chars (a-z, 0-9, _).",
    validationUsernameTaken = "This username is already taken. Please choose another.",

    navHome = "Home",
    navServices = "Services",
    navMessages = "Messages",
    navBookings = "Bookings",
    navProfile = "Profile",

    statusOnline = "ONLINE",
    statusBusy = "BUSY",
    statusOffline = "OFFLINE",
    statusOnlineDesc = "Yusuf Electronics is available for service & support.",
    statusBusyOrOfflineBanner = "Thank you for contacting Yusuf Electronics. We are currently busy or unavailable. Please leave your message. We will reply as soon as possible.",

    welcomePrefix = "Welcome,",
    searchPlaceholder = "Search fan, AC, CCTV, wiring, plumbing...",
    promoTitle = "Fast & Certified Home Electrical Experts",
    promoSubtitle = "All electrical, AC, IPS, CCTV & plumbing services managed in one place.",
    promoCta = "Book a Service",
    popularServices = "Popular Services",
    allServices = "All Service Categories",
    seeAll = "See All",
    emergencyContact = "Emergency Contact",
    contactYusufBtn = "Contact Yusuf Electronics",
    recentBookings = "Recent Bookings",
    customerReviews = "Customer Reviews",
    noBookingsYet = "No bookings yet. Tap any service to book a technician.",
    noReviewsYet = "No customer reviews published yet.",
    catalogEmptyMessage = "Service catalog is awaiting initialization by admin. You can message us directly anytime.",

    categoriesTitle = "All Services",
    allCategoriesChip = "All",
    availableBadge = "Available",
    unavailableBadge = "Unavailable",
    bookServiceBtn = "Book Service",
    contactBtn = "Contact",
    serviceDetailsTitle = "Service Details",
    featuresTitle = "What's Included",
    estimatedPriceTitle = "Estimated Pricing",
    sendMessageBtn = "Send Message",
    callNowBtn = "Call Now",
    noSearchResults = "No matching services found. Try another keyword or message us directly.",

    bookServiceTitle = "Book This Service",
    customerNameField = "Customer Name",
    phoneNumberField = "Phone Number",
    serviceCategoryField = "Service Category",
    selectedServiceField = "Selected Service",
    completeAddressField = "Complete Address",
    cityAreaField = "City / Area",
    problemDescField = "Detailed Problem Description",
    problemImagesLabel = "Optional Problem Photos (max 3)",
    addPhotoBtn = "Add Photo",
    preferredDateField = "Preferred Date",
    preferredTimeField = "Preferred Time",
    submitBookingBtn = "Submit Booking",
    bookingSubmittedTitle = "Booking Request Submitted",
    bookingSubmittedMsg = "Your unique booking ID has been saved and awaits admin acceptance.",
    myBookingsTitle = "My Bookings",
    cancelBookingBtn = "Cancel",
    writeReviewBtn = "Rate & Review",
    reviewedBadge = "Reviewed",
    adminNotesLabel = "Admin Notes",
    validationBookingFields = "Please fill in name, phone, address, city/area, description, date, and time.",

    messagesTitle = "Live Support Chat",
    typeMessageHint = "Write a message...",
    sendBtn = "Send",
    attachPhotoDesc = "Attach photo",
    chatEmptyHint = "Send a text or photo describing your electrical issue anytime.",
    readLabel = "Read",
    sentLabel = "Sent",

    notificationsTitle = "Notifications",
    noNotificationsYet = "No notifications yet.",

    contactUsTitle = "Contact & Business Info",
    phoneLabel = "Phone Number",
    whatsappLabel = "WhatsApp Number",
    addressContactLabel = "Business Address",
    workingHoursLabel = "Working Hours",
    serviceAreasLabel = "Service Areas",
    notConfiguredYet = "Not configured by admin yet",
    whatsappBtn = "WhatsApp",
    viewLocationBtn = "View Location",

    rateServiceTitle = "Rate Completed Service",
    ratingPrompt = "How was the service? (1–5 stars)",
    reviewCommentHint = "Share your experience...",
    submitReviewBtn = "Submit Review",

    profileTitle = "My Profile",
    editProfileBtn = "Edit Profile",
    accountCreatedLabel = "Member Since",
    notificationSettingsLabel = "Enable Notifications",
    privacyPolicyTitle = "Privacy Policy",
    termsTitle = "Terms & Conditions",
    logoutBtn = "Log Out",
    deleteAccountBtn = "Delete Account",
    deleteAccountConfirmTitle = "Delete Account?",
    deleteAccountConfirmBody = "This will permanently delete your profile and username registration. Are you sure?",
    confirmBtn = "Confirm",
    cancelBtn = "Cancel",
    retryBtn = "Retry",
    privacyPolicyBody = "Yusuf Electronics collects only the minimum information required to deliver electrical and home repair services (name, email, optional phone number, and service address). We never sell your personal data to third parties. All conversations and bookings are isolated to your account and authorized Yusuf Electronics administrators. You may delete your account at any time from your Profile.",
    termsBody = "Submitting a booking creates a pending service request subject to administrator review and scheduling. Final service charges depend on on-site diagnostics and parts used. Customers may cancel pending or scheduled bookings prior to service execution.",

    adminConsoleTitle = "Admin Control Console",
    adminBadge = "Admin"
)

val LocalAppStrings = compositionLocalOf { BanglaStrings }

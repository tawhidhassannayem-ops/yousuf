const { initializeApp, applicationDefault } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");

const DATABASE_ID =
  process.env.FIRESTORE_DATABASE_ID ||
  "ai-studio-android-yusufele-ed454ba4-c903-41a3-8b91-89d69af7ea53";

initializeApp({
  credential: applicationDefault(),
  projectId: process.env.FIREBASE_PROJECT_ID || "gen-lang-client-0060106260",
});

const db = getFirestore(DATABASE_ID);

const CATEGORIES = [
  ["cat_fan", "Fan Repair & Installation", "ফ্যান মেরামত ও ইনস্টলেশন", "Ceiling fan, table fan, capacitor replacement & maintenance", "সিলিং ফ্যান, টেবিল ফ্যান, ক্যাপাসিটর পরিবর্তন ও রক্ষণাবেক্ষণ", "fan", 1],
  ["cat_lighting", "Lighting Services", "লাইটিং সার্ভিস", "LED light repair, bulb, tube & decorative lighting setup", "এলইডি লাইট, টিউব লাইট ও ডেকোরেটিভ লাইটিং স্থাপন", "lighting", 2],
  ["cat_ac", "Air Conditioner Services", "এসি সার্ভিস", "AC installation, repair, deep cleaning & gas troubleshooting", "এসি ইনস্টলেশন, মেরামত, সার্ভিসিং ও ট্রাবলশুটিং", "ac", 3],
  ["cat_wiring", "Electrical Wiring Services", "ইলেকট্রিক্যাল ওয়্যারিং", "Home wiring, switches, sockets, DB board & circuit breakers", "বাসার ওয়্যারিং, সুইচ, সকেট, ডিবি বোর্ড ও ব্রেকার", "wiring", 4],
  ["cat_cctv", "CCTV Camera Services", "সিসিটিভি ক্যামেরা সার্ভিস", "CCTV camera setup, DVR/NVR configuration & remote viewing", "সিসিটিভি ক্যামেরা, ডিভিআর/এনভিআর সেটআপ ও রিমোট মনিটরিং", "cctv", 5],
  ["cat_plumbing", "Plumbing & Pipeline Services", "প্লাম্বিং ও পাইপলাইন", "Water pipeline, leakage repair, bathroom & kitchen plumbing", "পানির লাইন, পাইপ লিকেজ মেরামত, বাথরুম ও কিচেন প্লাম্বিং", "plumbing", 6],
  ["cat_charger", "Charger & Adapter Repair", "চার্জার ও অ্যাডাপ্টার মেরামত", "Mobile charger, laptop adapter & DC power supply repair", "মোবাইল চার্জার, পাওয়ার অ্যাডাপ্টার ও পাওয়ার সাপ্লাই মেরামত", "charger", 7],
  ["cat_audio", "Headphone & Earphone Repair", "হেডফোন ও ইয়ারফোন মেরামত", "Headphone jack, cable replacement & audio troubleshooting", "হেডফোন, ইয়ারফোন ও অডিও কেবল মেরামত", "audio", 8],
  ["cat_ips_ups", "IPS & UPS Services", "আইপিএস ও ইউপিএস সার্ভিস", "IPS/UPS installation, inverter battery & backup troubleshooting", "আইপিএস/ইউপিএস ইনস্টলেশন, ব্যাটারি সার্ভিস ও মেরামত", "ips", 9],
  ["cat_motor_pump", "Water Pump & Motor Repair", "পানির পাম্প ও মোটর মেরামত", "Submersible/surface water pump, motor winding & pressure fix", "ওয়াটার পাম্প, মোটর কয়েল ওয়াইন্ডিং ও পানির প্রেসার সমাধান", "pump", 10],
  ["cat_generator", "Generator Support & Maintenance", "জেনারেটর সাপোর্ট ও রক্ষণাবেক্ষণ", "Generator servicing, changeover switch & voltage troubleshooting", "জেনারেটর সার্ভিসিং, চেঞ্জওভার সুইচ ও লোড সমাধান", "generator", 11],
  ["cat_home_elec", "Home Electrical Maintenance", "হোম ইলেকট্রিক্যাল রক্ষণাবেক্ষণ", "Full home safety inspection, earthing check & fault finding", "সম্পূর্ণ বাসার ইলেকট্রিক্যাল চেকআপ ও আর্থিং সমাধান", "home", 12],
  ["cat_custom_elec", "Custom Electronics Repair", "কাস্টম ইলেকট্রনিক্স মেরামত", "Circuit board repair, stabilizer & specialized electronics diagnostics", "সার্কিট বোর্ড, স্ট্যাবিলাইজার ও অন্যান্য ইলেকট্রনিক্স মেরামত", "electronics", 13],
];

const SERVICES = [
  ["svc_fan_full", "cat_fan", "Fan Repair & Installation", "ফ্যান মেরামত ও ইনস্টলেশন", "Complete ceiling and table fan installation, capacitor swap, bearing & regulator servicing.", "সিলিং ও টেবিল ফ্যান স্থাপন, ক্যাপাসিটর পরিবর্তন, বিয়ারিং ও রেগুলেটর মেরামত।", ["Ceiling Fan Repair", "Table Fan Repair", "Capacitor Replacement", "Fan Installation", "Fan Maintenance"], ["সিলিং ফ্যান মেরামত", "টেবিল ফ্যান মেরামত", "ক্যাপাসিটর পরিবর্তন", "ফ্যান ইনস্টলেশন", "ফ্যান রক্ষণাবেক্ষণ"], 300, "৳300 - ৳1,200", "৳৩০০ - ৳১,২০০", true],
  ["svc_lighting_full", "cat_lighting", "LED & Decorative Lighting", "এলইডি ও ডেকোরেটিভ লাইটিং", "Indoor/outdoor LED light repair, tube light setup & decorative home lighting.", "বাসা ও অফিসের এলইডি লাইট মেরামত, টিউব লাইট স্থাপন ও ডেকোরেটিভ লাইটিং।", ["LED Light Repair", "Bulb Replacement", "Tube Light Installation", "Decorative Lighting", "Indoor and Outdoor Lighting"], ["এলইডি লাইট মেরামত", "বাল্ব পরিবর্তন", "টিউব লাইট ইনস্টলেশন", "ডেকোরেটিভ লাইটিং", "ইনডোর ও আউটডোর লাইটিং"], 250, "৳250 - ৳1,500", "৳২৫০ - ৳১,৫০০", true],
  ["svc_ac_full", "cat_ac", "AC Installation & Master Servicing", "এসি ইনস্টলেশন ও সার্ভিসিং", "Split & window AC installation, deep jet wash, cooling check & repair.", "স্প্লিট ও উইন্ডো এসি ইনস্টলেশন, জেট ওয়াশ ক্লিনিং, কুলিং ও কম্প্রেসার মেরামত।", ["AC Installation", "AC Repair", "AC Cleaning", "AC Maintenance", "AC Troubleshooting"], ["এসি ইনস্টলেশন", "এসি মেরামত", "এসি ক্লিনিং", "এসি রক্ষণাবেক্ষণ", "এসি ট্রাবলশুটিং"], 1200, "৳1,200 - ৳4,500", "৳১,২০০ - ৳৪,৫০০", true],
  ["svc_wiring_full", "cat_wiring", "Home Wiring & Distribution Board", "হোম ওয়্যারিং ও ডিবি বোর্ড", "Safe concealed/surface wiring, switches, sockets, DB board & circuit breakers.", "নিরাপদ বাসার ওয়্যারিং, সুইচ-সকেট স্থাপন, ডিবি বোর্ড ও সার্কিট ব্রেকার।", ["Home Wiring", "Switch Installation", "Socket Repair", "Distribution Board", "Circuit Breaker", "Electrical Troubleshooting"], ["হোম ওয়্যারিং", "সুইচ ইনস্টলেশন", "সকেট মেরামত", "ডিস্ট্রিবিউশন বোর্ড", "সার্কিট ব্রেকার", "ইলেকট্রিক্যাল ট্রাবলশুটিং"], 500, "৳500+", "৳৫০০+", true],
  ["svc_cctv_full", "cat_cctv", "CCTV Camera & DVR/NVR Setup", "সিসিটিভি ক্যামেরা ও ডিভিআর সেটআপ", "HD/IP CCTV installation, DVR/NVR storage configuration & mobile remote viewing.", "এইচডি/আইপি সিসিটিভি ক্যামেরা স্থাপন, ডিভিআর/এনভিআর কনফিগারেশন ও মোবাইলে লাইভ ভিউ।", ["CCTV Installation", "Camera Configuration", "DVR/NVR Setup", "Camera Repair", "Remote Monitoring Setup"], ["সিসিটিভি ইনস্টলেশন", "ক্যামেরা কনফিগারেশন", "ডিভিআর/এনভিআর সেটআপ", "ক্যামেরা মেরামত", "রিমোট মনিটরিং সেটআপ"], 1500, "৳1,500+", "৳১,৫০০+", true],
  ["svc_plumbing_full", "cat_plumbing", "Plumbing & Water Pipeline Fix", "প্লাম্বিং ও পানির পাইপলাইন", "Water pipeline installation, leakage sealing, kitchen/bathroom fittings & tank lines.", "পানির পাইপলাইন স্থাপন, লিকেজ মেরামত, বাথরুম-কিচেন ফিটিং ও ট্যাংক কানেকশন।", ["Water Pipeline Installation", "Pipe Leakage Repair", "Bathroom Plumbing", "Kitchen Plumbing", "Water Tank Connection"], ["ওয়াটার পাইপলাইন স্থাপন", "পাইপ লিকেজ মেরামত", "বাথরুম প্লাম্বিং", "কিচেন প্লাম্বিং", "ওয়াটার ট্যাংক কানেকশন"], 500, "৳500 - ৳2,500", "৳৫০০ - ৳২,৫০০", true],
  ["svc_charger_full", "cat_charger", "Charger & Power Adapter Repair", "চার্জার ও পাওয়ার অ্যাডাপ্টার মেরামত", "Precision diagnostics and repair for mobile chargers, laptop adapters & power supplies.", "মোবাইল চার্জার, ল্যাপটপ অ্যাডাপ্টার ও ডিসি পাওয়ার সাপ্লাই মেরামত।", ["Mobile Charger Repair", "Power Adapter Repair", "Charging Problems", "Power Supply Repair"], ["মোবাইল চার্জার মেরামত", "পাওয়ার অ্যাডাপ্টার মেরামত", "চার্জিং সমস্যা সমাধান", "পাওয়ার সাপ্লাই মেরামত"], 200, "৳200 - ৳800", "৳২০০ - ৳৮০০", false],
  ["svc_audio_full", "cat_audio", "Headphone & Earphone Repair", "হেডফোন ও ইয়ারফোন মেরামত", "Audio jack soldering, cable replacement & driver troubleshooting.", "হেডফোন ও ইয়ারফোনের জ্যাক, কেবল পরিবর্তন ও সাউন্ড সমস্যা সমাধান।", ["Headphone Repair", "Earphone Repair", "Cable Replacement", "Audio Troubleshooting"], ["হেডফোন মেরামত", "ইয়ারফোন মেরামত", "কেবল পরিবর্তন", "অডিও ট্রাবলশুটিং"], 200, "৳200 - ৳700", "৳২০০ - ৳৭০০", false],
  ["svc_ips_full", "cat_ips_ups", "IPS & UPS Installation & Repair", "আইপিএস ও ইউপিএস ইনস্টলেশন ও মেরামত", "Complete IPS/UPS installation, inverter PCB repair, battery wiring & backup setup.", "আইপিএস ও ইউপিএস স্থাপন, সার্কিট মেরামত, ব্যাটারি সার্ভিস ও ব্যাকআপ সংযোগ।", ["IPS Installation", "UPS Repair", "Battery Setup", "Power Backup Troubleshooting"], ["আইপিএস ইনস্টলেশন", "ইউপিএস মেরামত", "ব্যাটারি সেটআপ", "পাওয়ার ব্যাকআপ সমাধান"], 800, "৳800 - ৳3,000", "৳৮০০ - ৳৩,০০০", true],
  ["svc_pump_full", "cat_motor_pump", "Water Pump & Motor Repair", "পানির পাম্প ও মোটর মেরামত", "Water pump motor winding, capacitor fix, automatic float switch & pressure restoration.", "পানির পাম্প ও মোটর মেরামত, অটো সুইচ স্থাপন ও পানির প্রেসার সমস্যা সমাধান।", ["Water Pump Repair", "Motor Servicing", "Water Pressure Issues"], ["পানির পাম্প মেরামত", "মোটর সার্ভিসিং", "পানির প্রেসার সমস্যা সমাধান"], 700, "৳700 - ৳2,800", "৳৭০০ - ৳২,৮০০", false],
  ["svc_gen_full", "cat_generator", "Generator Support & ATS Setup", "জেনারেটর সাপোর্ট ও রক্ষণাবেক্ষণ", "Generator maintenance, ATS/changeover switch setup & emergency fault checks.", "জেনারেটর রক্ষণাবেক্ষণ, চেঞ্জওভার সুইচ ও বৈদ্যুতিক ত্রুটি সমাধান।", ["Generator Maintenance", "Generator Troubleshooting", "Emergency Support"], ["জেনারেটর রক্ষণাবেক্ষণ", "জেনারেটর ট্রাবলশুটিং", "জরুরি সাপোর্ট"], 1500, "৳1,500+", "৳১,৫০০+", false],
  ["svc_home_elec_full", "cat_home_elec", "Home Electrical Safety Checkup", "বাসার ইলেকট্রিক্যাল সেফটি চেকআপ", "Whole-home electrical inspection, earthing verification & preventive maintenance.", "সম্পূর্ণ বাসার ইলেকট্রিক্যাল নিরাপত্তা পরীক্ষা, আর্থিং ও লোড ব্যালেন্সিং।", ["Full Home Electrical Checkup", "Fault Detection", "Electrical Maintenance"], ["সম্পূর্ণ বাসার ইলেকট্রিক্যাল চেকআপ", "ফল্ট নির্ণয়", "ইলেকট্রিক্যাল রক্ষণাবেক্ষণ"], 600, "৳600 - ৳2,000", "৳৬০০ - ৳২,০০০", false],
  ["svc_custom_elec_full", "cat_custom_elec", "Custom Electronics & PCB Repair", "কাস্টম ইলেকট্রনিক্স ও পিসিবি মেরামত", "Diagnostic bench repair for voltage stabilizers, control boards & home appliances.", "ভোল্টেজ স্ট্যাবিলাইজার, কন্ট্রোল বোর্ড ও বিশেষ ইলেকট্রনিক্স যন্ত্রপাতি মেরামত।", ["Circuit Board Repair", "Electronic Appliance Diagnostics", "Custom Electronics Solutions"], ["সার্কিট বোর্ড মেরামত", "ইলেকট্রনিক্স ডায়াগনস্টিকস", "কাস্টম ইলেকট্রনিক্স সমাধান"], 400, "Inspection quote", "পরিদর্শন সাপেক্ষে", false],
];

async function runSeed() {
  const batch = db.batch();

  CATEGORIES.forEach(([id, nameEn, nameBn, descEn, descBn, iconKey, sortOrder]) => {
    batch.set(
      db.collection("service_categories").doc(id),
      {
        nameEn,
        nameBn,
        descriptionEn: descEn,
        descriptionBn: descBn,
        iconKey,
        status: "active",
        sortOrder,
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  });

  SERVICES.forEach(([id, categoryId, nameEn, nameBn, descEn, descBn, featuresEn, featuresBn, price, priceDisplayEn, priceDisplayBn, isPopular]) => {
    batch.set(
      db.collection("services").doc(id),
      {
        categoryId,
        nameEn,
        nameBn,
        descriptionEn: descEn,
        descriptionBn: descBn,
        featuresEn,
        featuresBn,
        price,
        priceType: "range",
        priceDisplayEn,
        priceDisplayBn,
        availability: "available",
        isPopular,
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  });

  const bizRef = db.collection("business_settings").doc("main");
  const bizSnap = await bizRef.get();
  if (!bizSnap.exists) {
    batch.set(bizRef, {
      businessName: "YUSUF ELECTRONICS",
      phoneNumber: "",
      whatsappNumber: "",
      email: "",
      addressEn: "",
      addressBn: "",
      mapsQueryUrl: "",
      workingHoursEn: "Saturday - Thursday: 9:00 AM - 9:00 PM",
      workingHoursBn: "শনিবার - বৃহস্পতিবার: সকাল ৯টা - রাত ৯টা",
      availabilityStatus: "online",
      serviceAreasEn: "Local City & Surrounding Areas",
      serviceAreasBn: "শহর ও আশেপাশের এলাকা",
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  await batch.commit();
  console.log(`Seeded ${CATEGORIES.length} categories and ${SERVICES.length} services into database ${DATABASE_ID}.`);
}

runSeed().catch((err) => {
  console.error("Seed failed:", err);
  process.exit(1);
});

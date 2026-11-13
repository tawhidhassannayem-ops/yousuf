package com.example.data.seed

import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceItem
import com.google.firebase.firestore.FieldValue

object DefaultCatalogData {

    data class CategorySeed(
        val id: String,
        val nameEn: String,
        val nameBn: String,
        val descriptionEn: String,
        val descriptionBn: String,
        val iconKey: String,
        val sortOrder: Long
    )

    data class ServiceSeed(
        val id: String,
        val categoryId: String,
        val nameEn: String,
        val nameBn: String,
        val descriptionEn: String,
        val descriptionBn: String,
        val featuresEn: List<String>,
        val featuresBn: List<String>,
        val price: Double,
        val priceType: String,
        val priceDisplayEn: String,
        val priceDisplayBn: String,
        val isPopular: Boolean
    )

    val categories: List<CategorySeed> = listOf(
        CategorySeed(
            id = "cat_fan",
            nameEn = "Fan Repair & Installation",
            nameBn = "ফ্যান মেরামত ও ইনস্টলেশন",
            descriptionEn = "Ceiling fan, table fan repair, capacitor change, and balanced installation.",
            descriptionBn = "সিলিং ফ্যান ও টেবিল ফ্যান মেরামত, ক্যাপাসিটর পরিবর্তন এবং নিরাপদ ইনস্টলেশন।",
            iconKey = "fan",
            sortOrder = 1L
        ),
        CategorySeed(
            id = "cat_lighting",
            nameEn = "Lighting Services",
            nameBn = "লাইটিং সার্ভিস",
            descriptionEn = "LED light repair, bulb & tube light fitting, indoor and decorative lighting.",
            descriptionBn = "এলইডি লাইট মেরামত, বাল্ব ও টিউব লাইট স্থাপন, ইনডোর ও ডেকোরেটিভ লাইটিং।",
            iconKey = "lighting",
            sortOrder = 2L
        ),
        CategorySeed(
            id = "cat_ac",
            nameEn = "Air Conditioner Services",
            nameBn = "এসি সার্ভিসিং ও মেরামত",
            descriptionEn = "AC installation, jet cleaning, gas check, repair, and seasonal maintenance.",
            descriptionBn = "এসি ইনস্টলেশন, জেট ক্লিনিং, গ্যাস চেক, মেরামত ও নিয়মিত রক্ষণাবেক্ষণ।",
            iconKey = "ac",
            sortOrder = 3L
        ),
        CategorySeed(
            id = "cat_wiring",
            nameEn = "Electrical Wiring Services",
            nameBn = "ইলেকট্রিক্যাল ওয়্যারিং সার্ভিস",
            descriptionEn = "Home wiring, switchboard, socket, circuit breaker, and distribution board setup.",
            descriptionBn = "বাসার ওয়্যারিং, সুইচ ও সকেট মেরামত, সার্কিট ব্রেকার ও ডিবি বোর্ড সেটআপ।",
            iconKey = "wiring",
            sortOrder = 4L
        ),
        CategorySeed(
            id = "cat_cctv",
            nameEn = "CCTV Camera Services",
            nameBn = "সিসিটিভি ক্যামেরা সার্ভিস",
            descriptionEn = "CCTV installation, DVR/NVR setup, camera repair, and mobile remote viewing.",
            descriptionBn = "সিসিটিভি ক্যামেরা স্থাপন, ডিভিআর/এনভিআর সেটআপ, মেরামত ও মোবাইল মনিটরিং।",
            iconKey = "cctv",
            sortOrder = 5L
        ),
        CategorySeed(
            id = "cat_plumbing",
            nameEn = "Plumbing & Pipeline Services",
            nameBn = "প্লাম্বিং ও পাইপলাইন সার্ভিস",
            descriptionEn = "Water pipeline fitting, leakage repair, bathroom, kitchen, and water tank connection.",
            descriptionBn = "পানির পাইপলাইন, লিকেজ মেরামত, বাথরুম, কিচেন ও পানির ট্যাংক সংযোগ।",
            iconKey = "plumbing",
            sortOrder = 6L
        ),
        CategorySeed(
            id = "cat_charger",
            nameEn = "Charger & Adapter Repair",
            nameBn = "চার্জার ও অ্যাডাপ্টার মেরামত",
            descriptionEn = "Mobile charger, laptop power adapter, DC supply, and charging port troubleshooting.",
            descriptionBn = "মোবাইল চার্জার, ল্যাপটপ অ্যাডাপ্টার ও পাওয়ার সাপ্লাই মেরামত।",
            iconKey = "charger",
            sortOrder = 7L
        ),
        CategorySeed(
            id = "cat_audio",
            nameEn = "Headphone & Earphone Repair",
            nameBn = "হেডফোন ও ইয়ারফোন মেরামত",
            descriptionEn = "Headphone, earphone jack/cable replacement, and audio channel repair.",
            descriptionBn = "হেডফোন ও ইয়ারফোন মেরামত, ক্যাবল পরিবর্তন ও অডিও সমস্যা সমাধান।",
            iconKey = "audio",
            sortOrder = 8L
        ),
        CategorySeed(
            id = "cat_ips",
            nameEn = "IPS & UPS Services",
            nameBn = "আইপিএস ও ইউপিএস সার্ভিস",
            descriptionEn = "IPS installation, UPS repair, battery replacement, and backup power maintenance.",
            descriptionBn = "আইপিএস ইনস্টলেশন, ইউপিএস মেরামত, ব্যাটারি পরিবর্তন ও ব্যাকআপ সিস্টেম সার্ভিস।",
            iconKey = "ips",
            sortOrder = 9L
        ),
        CategorySeed(
            id = "cat_pump",
            nameEn = "Water Pump & Motor Services",
            nameBn = "ওয়াটার পাম্প ও মোটর সার্ভিস",
            descriptionEn = "Water pump installation, coil & bearing motor repair, and automatic switch setup.",
            descriptionBn = "পানির পাম্প স্থাপন, মোটর মেরামত ও অটো সুইচ রক্ষণাবেক্ষণ।",
            iconKey = "pump",
            sortOrder = 10L
        ),
        CategorySeed(
            id = "cat_generator",
            nameEn = "Generator Services",
            nameBn = "জেনারেটর সার্ভিস",
            descriptionEn = "Generator wiring, changeover switch setup, servicing, and fault diagnostics.",
            descriptionBn = "জেনারেটর সংযোগ, চেঞ্জওভার সুইচ ও নিয়মিত সার্ভিসিং।",
            iconKey = "generator",
            sortOrder = 11L
        ),
        CategorySeed(
            id = "cat_home_electrical",
            nameEn = "Complete Home Electrical Services",
            nameBn = "সম্পূর্ণ হোম ইলেকট্রিক্যাল সার্ভিস",
            descriptionEn = "Full apartment or house electrical setup, safety earthing, and load inspection.",
            descriptionBn = "বাসাবাড়ির সম্পূর্ণ ইলেকট্রিক্যাল সেটআপ, আর্থিং ও নিরাপত্তা পরিদর্শন।",
            iconKey = "home",
            sortOrder = 12L
        ),
        CategorySeed(
            id = "cat_other_electronics",
            nameEn = "Other Electronics Repair Services",
            nameBn = "অন্যান্য ইলেকট্রনিক্স মেরামত",
            descriptionEn = "Voltage stabilizer, home appliance circuits, and general electronics diagnostics.",
            descriptionBn = "ভোল্টেজ স্ট্যাবিলাইজার ও অন্যান্য ইলেকট্রনিক্স যন্ত্রপাতি মেরামত।",
            iconKey = "electronics",
            sortOrder = 13L
        )
    )

    val services: List<ServiceSeed> = listOf(
        // 1. Fan Repair & Installation
        ServiceSeed(
            id = "svc_ceiling_fan_repair",
            categoryId = "cat_fan",
            nameEn = "Ceiling Fan Repair & Capacitor Replacement",
            nameBn = "সিলিং ফ্যান মেরামত ও ক্যাপাসিটর পরিবর্তন",
            descriptionEn = "Complete diagnosis for slow speed, humming noise, bearing friction, or burnt capacitor in ceiling fans.",
            descriptionBn = "সিলিং ফ্যান ধীরে ঘোরা, শব্দ হওয়া, বিয়ারিং বা ক্যাপাসিটরের সমস্যার সম্পূর্ণ সমাধান।",
            featuresEn = listOf("Speed & voltage check", "Quality capacitor replacement", "Bearing & shaft lubrication", "7-day service warranty"),
            featuresBn = listOf("স্পিড ও ভোল্টেজ পরীক্ষা", "মানসম্মত ক্যাপাসিটর পরিবর্তন", "বিয়ারিং ও শ্যাফট লুব্রিকেশন", "৭ দিনের সার্ভিস ওয়ারেন্টি"),
            price = 250.0,
            priceType = "estimate",
            priceDisplayEn = "Inspection & repair on quote",
            priceDisplayBn = "পরিদর্শন সাপেক্ষে চার্জ নির্ধারিত",
            isPopular = true
        ),
        ServiceSeed(
            id = "svc_fan_installation",
            categoryId = "cat_fan",
            nameEn = "Ceiling & Table Fan Installation & Maintenance",
            nameBn = "ফ্যান ইনস্টলেশন ও টেবিল ফ্যান সার্ভিসিং",
            descriptionEn = "Safe mounting of new ceiling fans, regulator wiring, and table fan servicing.",
            descriptionBn = "নতুন সিলিং ফ্যান স্থাপন, রেগুলেটর সংযোগ ও টেবিল ফ্যান সার্ভিসিং।",
            featuresEn = listOf("Vibration-free mounting", "Dimmer/regulator fitting", "Table fan motor tuning"),
            featuresBn = listOf("কম্পনমুক্ত নিরাপদ ফিটিং", "রেগুলেটর সংযোগ", "টেবিল ফ্যান মোটর টিউনিং"),
            price = 300.0,
            priceType = "estimate",
            priceDisplayEn = "Estimate upon inspection",
            priceDisplayBn = "পরিদর্শন শেষে নির্ধারিত মূল্য",
            isPopular = false
        ),
        // 2. Lighting Services
        ServiceSeed(
            id = "svc_led_tube_lighting",
            categoryId = "cat_lighting",
            nameEn = "LED Light, Bulb & Tube Light Installation",
            nameBn = "এলইডি লাইট ও টিউব লাইট মেরামত ও স্থাপন",
            descriptionEn = "Repair and replacement of flickering LED panels, tube lights, holders, and outdoor floodlights.",
            descriptionBn = "ফ্লিকারিং এলইডি লাইট, টিউব লাইট, হোল্ডার ও আউটডোর লাইট মেরামত ও স্থাপন।",
            featuresEn = listOf("Driver & holder testing", "Energy-saving LED fitting", "Indoor & outdoor wiring safety"),
            featuresBn = listOf("ড্রাইভার ও হোল্ডার পরীক্ষা", "সাশ্রয়ী এলইডি ফিটিং", "ইনডোর ও আউটডোর নিরাপদ সংযোগ"),
            price = 200.0,
            priceType = "estimate",
            priceDisplayEn = "Based on fixture count",
            priceDisplayBn = "লাইটের সংখ্যা অনুযায়ী মূল্য",
            isPopular = true
        ),
        ServiceSeed(
            id = "svc_decorative_lighting",
            categoryId = "cat_lighting",
            nameEn = "Decorative & Chandelier Lighting Setup",
            nameBn = "ডেকোরেটিভ ও ইনডোর-আউটডোর লাইটিং",
            descriptionEn = "Strip lights, ceiling spotlights, chandelier mounting, and balcony/gate lighting.",
            descriptionBn = "স্ট্রিপ লাইট, সিলিং স্পটলাইট, ঝাড়বাতি এবং বারান্দা ও গেটের আলোকসজ্জা।",
            featuresEn = listOf("Concealed adapter setup", "Warm/cool lighting layout", "Clean cable management"),
            featuresBn = listOf("গোপন অ্যাডাপ্টার সেটআপ", "সুন্দর লাইটিং বিন্যাস", "পরিচ্ছন্ন ক্যাবল ম্যানেজমেন্ট"),
            price = 500.0,
            priceType = "estimate",
            priceDisplayEn = "Custom site estimate",
            priceDisplayBn = "কাজের ধরন অনুযায়ী মূল্য",
            isPopular = false
        ),
        // 3. Air Conditioner Services
        ServiceSeed(
            id = "svc_ac_repair_cleaning",
            categoryId = "cat_ac",
            nameEn = "AC Cleaning, Repair & Troubleshooting",
            nameBn = "এসি ক্লিনিং, মেরামত ও ট্রাবলশুটিং",
            descriptionEn = "Deep jet wash, cooling issue diagnosis, water leakage fix, and compressor/PCB check.",
            descriptionBn = "এসি ডিপ জেট ওয়াশ, ঠান্ডা না হওয়া বা পানি পড়ার সমাধান এবং কম্প্রেসর পরীক্ষা।",
            featuresEn = listOf("Indoor & outdoor jet wash", "Drain pipe & filter cleaning", "Cooling & gas pressure test"),
            featuresBn = listOf("ইনডোর ও আউটডোর জেট ওয়াশ", "ড্রেইন পাইপ ও ফিল্টার পরিষ্কার", "কুলিং ও গ্যাস প্রেসার পরীক্ষা"),
            price = 800.0,
            priceType = "estimate",
            priceDisplayEn = "Depends on AC capacity",
            priceDisplayBn = "এসির টন ও কাজের ওপর নির্ভরশীল",
            isPopular = true
        ),
        ServiceSeed(
            id = "svc_ac_installation",
            categoryId = "cat_ac",
            nameEn = "Split & Window AC Installation & Maintenance",
            nameBn = "এসি ইনস্টলেশন ও নিয়মিত রক্ষণাবেক্ষণ",
            descriptionEn = "Professional split/window AC mounting, copper pipe flaring, vacuuming, and power breaker setup.",
            descriptionBn = "স্প্লিট ও উইন্ডো এসি স্থাপন, কপার পাইপ ফিটিং, ভ্যাকুয়াম ও পাওয়ার ব্রেকার সংযোগ।",
            featuresEn = listOf("Levelled bracket mounting", "Copper pipe & insulation check", "Dedicated circuit safety"),
            featuresBn = listOf("সঠিক লেভেলিং ব্র্যাকেট স্থাপন", "কপার পাইপ ও ইনসুলেশন চেক", "আলাদা সার্কিট সুরক্ষা"),
            price = 1500.0,
            priceType = "estimate",
            priceDisplayEn = "Site survey estimate",
            priceDisplayBn = "স্থান পরিদর্শন সাপেক্ষে মূল্য",
            isPopular = false
        ),
        // 4. Electrical Wiring Services
        ServiceSeed(
            id = "svc_home_wiring_breaker",
            categoryId = "cat_wiring",
            nameEn = "Home Wiring, Distribution Board & Circuit Breaker",
            nameBn = "হোম ওয়্যারিং, ডিবি বোর্ড ও সার্কিট ব্রেকার",
            descriptionEn = "Short-circuit troubleshooting, MCB/RCCB installation, switch-socket repair, and full room rewiring.",
            descriptionBn = "শর্ট-সার্কিট সমাধান, এমসিবি/আরসিসিবি স্থাপন, সুইচ-সকেট মেরামত ও নতুন ওয়্যারিং।",
            featuresEn = listOf("Short-circuit fault isolation", "MCB/RCCB load balancing", "Switch & power socket replacement"),
            featuresBn = listOf("শর্ট-সার্কিট ফল্ট শনাক্তকরণ", "এমসিবি/আরসিসিবি লোড ব্যালেন্সিং", "সুইচ ও সকেট পরিবর্তন"),
            price = 400.0,
            priceType = "estimate",
            priceDisplayEn = "Inspection & quote",
            priceDisplayBn = "পরিদর্শন সাপেক্ষে নির্ধারিত",
            isPopular = true
        ),
        // 5. CCTV Camera Services
        ServiceSeed(
            id = "svc_cctv_installation",
            categoryId = "cat_cctv",
            nameEn = "CCTV Installation, DVR/NVR & Remote Setup",
            nameBn = "সিসিটিভি ইনস্টলেশন, ডিভিআর ও মোবাইল মনিটরিং",
            descriptionEn = "HD/IP camera installation, DVR/NVR storage configuration, cable routing, and smartphone viewing.",
            descriptionBn = "এইচডি/আইপি ক্যামেরা স্থাপন, ডিভিআর/এনভিআর কনফিগারেশন ও মোবাইলে লাইভ দেখা।",
            featuresEn = listOf("Blind-spot coverage planning", "DVR/NVR hard disk setup", "Mobile app remote viewing"),
            featuresBn = listOf("সঠিক অ্যাঙ্গেল ও কাভারেজ পরিকল্পনা", "ডিভিআর/এনভিআর হার্ডডিস্ক সেটআপ", "মোবাইল অ্যাপে রিমোট ভিউ"),
            price = 1200.0,
            priceType = "estimate",
            priceDisplayEn = "Per camera & site plan",
            priceDisplayBn = "ক্যামেরার সংখ্যা অনুযায়ী নির্ধারিত",
            isPopular = true
        ),
        // 6. Plumbing & Pipeline Services
        ServiceSeed(
            id = "svc_plumbing_leakage",
            categoryId = "cat_plumbing",
            nameEn = "Pipe Leakage, Bathroom, Kitchen & Tank Plumbing",
            nameBn = "পাইপ লিকেজ, বাথরুম, কিচেন ও ট্যাংক প্লাম্বিং",
            descriptionEn = "Water pipeline fitting, tap/mixer repair, pipe leakage sealing, and rooftop water tank connections.",
            descriptionBn = "পানির পাইপলাইন স্থাপন, ট্যাপ ও মিক্সার মেরামত, লিকেজ বন্ধ ও ট্যাংক সংযোগ।",
            featuresEn = listOf("Pressure leak detection", "Bathroom & kitchen fittings", "Water tank float & outlet pipe"),
            featuresBn = listOf("লিকেজ শনাক্ত ও মেরামত", "বাথরুম ও কিচেন ফিটিং", "ট্যাংক ফ্লোট ও আউটলেট পাইপ"),
            price = 350.0,
            priceType = "estimate",
            priceDisplayEn = "Based on plumbing work",
            priceDisplayBn = "কাজের পরিধি অনুযায়ী নির্ধারিত",
            isPopular = true
        ),
        // 7. Charger & Adapter Repair
        ServiceSeed(
            id = "svc_charger_adapter",
            categoryId = "cat_charger",
            nameEn = "Mobile Charger, Adapter & Power Supply Repair",
            nameBn = "মোবাইল চার্জার, অ্যাডাপ্টার ও পাওয়ার সাপ্লাই মেরামত",
            descriptionEn = "Repair for laptop adapters, fast chargers, DC power supplies, and unstable voltage output.",
            descriptionBn = "ল্যাপটপ অ্যাডাপ্টার, মোবাইল চার্জার ও ডিসি পাওয়ার সাপ্লাইয়ের সমস্যা মেরামত।",
            featuresEn = listOf("DC cord & pin replacement", "Output voltage stability test", "Component-level troubleshooting"),
            featuresBn = listOf("ডিসি কর্ড ও পিন পরিবর্তন", "ভোল্টেজ আউটপুট পরীক্ষা", "কম্পোনেন্ট-লেভেল মেরামত"),
            price = 200.0,
            priceType = "estimate",
            priceDisplayEn = "After component check",
            priceDisplayBn = "যন্ত্রাংশ পরীক্ষার পর নির্ধারিত",
            isPopular = false
        ),
        // 8. Headphone & Earphone Repair
        ServiceSeed(
            id = "svc_headphone_repair",
            categoryId = "cat_audio",
            nameEn = "Headphone & Earphone Cable & Audio Repair",
            nameBn = "হেডফোন ও ইয়ারফোন ক্যাবল ও অডিও মেরামত",
            descriptionEn = "One-side sound issue, 3.5mm/Type-C jack replacement, soldering, and headband wiring fix.",
            descriptionBn = "এক পাশে শব্দ না আসা, জ্যাক/ক্যাবল পরিবর্তন ও সোল্ডারিং মেরামত।",
            featuresEn = listOf("Precision micro-soldering", "Jack & cable replacement", "Left/right audio balance test"),
            featuresBn = listOf("নিখুঁত মাইক্রো-সোল্ডারিং", "জ্যাক ও ক্যাবল পরিবর্তন", "বাম/ডান অডিও ব্যালেন্স পরীক্ষা"),
            price = 150.0,
            priceType = "estimate",
            priceDisplayEn = "After audio check",
            priceDisplayBn = "পরীক্ষার পর নির্ধারিত মূল্য",
            isPopular = false
        ),
        // 9. IPS & UPS Services
        ServiceSeed(
            id = "svc_ips_ups",
            categoryId = "cat_ips",
            nameEn = "IPS Installation, UPS Repair & Battery Service",
            nameBn = "আইপিএস ইনস্টলেশন, ইউপিএস মেরামত ও ব্যাটারি সার্ভিস",
            descriptionEn = "Full home IPS wiring, UPS inverter repair, battery terminal cleaning, and backup optimization.",
            descriptionBn = "বাসার আইপিএস ওয়্যারিং, ইউপিএস মেরামত, ব্যাটারি পরিবর্তন ও ব্যাকআপ রক্ষণাবেক্ষণ।",
            featuresEn = listOf("Dedicated IPS phase wiring", "Battery voltage & water check", "Overload & charging circuit test"),
            featuresBn = listOf("আলাদা আইপিএস ফেজ ওয়্যারিং", "ব্যাটারি ভোল্টেজ ও ওয়াটার চেক", "চার্জিং সার্কিট পরীক্ষা"),
            price = 600.0,
            priceType = "estimate",
            priceDisplayEn = "Inspection & quote",
            priceDisplayBn = "পরিদর্শন শেষে নির্ধারিত",
            isPopular = true
        ),
        // 10. Water Pump & Motor Services
        ServiceSeed(
            id = "svc_water_pump",
            categoryId = "cat_pump",
            nameEn = "Water Pump Installation & Motor Repair",
            nameBn = "ওয়াটার পাম্প স্থাপন ও মোটর মেরামত",
            descriptionEn = "Submersible & centrifugal water pump installation, motor winding check, seal & bearing repair.",
            descriptionBn = "সাবমার্সিবল ও পানির পাম্প স্থাপন, মোটর মেরামত, সিল ও বিয়ারিং সার্ভিসিং।",
            featuresEn = listOf("Suction & pressure line setup", "Motor coil & starter check", "Auto tank controller wiring"),
            featuresBn = listOf("সাকশন ও ডেলিভারি লাইন সেটআপ", "মোটর কয়েল ও স্টার্টার চেক", "অটো ট্যাংক কন্ট্রোলার সংযোগ"),
            price = 700.0,
            priceType = "estimate",
            priceDisplayEn = "Depends on HP & fault",
            priceDisplayBn = "মোটর ক্ষমতা অনুযায়ী নির্ধারিত",
            isPopular = false
        ),
        // 11. Generator Services
        ServiceSeed(
            id = "svc_generator",
            categoryId = "cat_generator",
            nameEn = "Generator Wiring, Changeover & Servicing",
            nameBn = "জেনারেটর সংযোগ, চেঞ্জওভার ও সার্ভিসিং",
            descriptionEn = "Safe changeover/ATS switch setup, alternator voltage testing, and routine maintenance.",
            descriptionBn = "নিরাপদ চেঞ্জওভার/এটিএস সুইচ স্থাপন, ভোল্টেজ পরীক্ষা ও সার্ভিসিং।",
            featuresEn = listOf("Changeover & ATS connection", "Load balancing & earthing check", "Fault diagnostics"),
            featuresBn = listOf("চেঞ্জওভার ও এটিএস সংযোগ", "লোড ব্যালেন্স ও আর্থিং চেক", "সমস্যা নির্ণয় ও সমাধান"),
            price = 1000.0,
            priceType = "estimate",
            priceDisplayEn = "Custom site estimate",
            priceDisplayBn = "পরিদর্শন অনুযায়ী মূল্য",
            isPopular = false
        ),
        // 12. Complete Home Electrical Services
        ServiceSeed(
            id = "svc_complete_home",
            categoryId = "cat_home_electrical",
            nameEn = "Complete Home Electrical Inspection & Setup",
            nameBn = "সম্পূর্ণ বাসার ইলেকট্রিক্যাল সেটআপ ও পরিদর্শন",
            descriptionEn = "End-to-end apartment/home electrical installation, earthing protection, and full safety audit.",
            descriptionBn = "নতুন বা পুরোনো বাসার সম্পূর্ণ ইলেকট্রিক্যাল কাজ, আর্থিং সুরক্ষা ও লোড অডিট।",
            featuresEn = listOf("Full load calculation", "Earthing & surge safety", "Main board to room wiring"),
            featuresBn = listOf("সম্পূর্ণ লোড ক্যালকুলেশন", "আর্থিং ও সার্জ সুরক্ষা", "মেইন বোর্ড থেকে রুম ওয়্যারিং"),
            price = 2000.0,
            priceType = "estimate",
            priceDisplayEn = "Project-based estimate",
            priceDisplayBn = "প্রজেক্ট পরিদর্শন সাপেক্ষে",
            isPopular = false
        ),
        // 13. Other Electronics Repair Services
        ServiceSeed(
            id = "svc_other_electronics",
            categoryId = "cat_other_electronics",
            nameEn = "Stabilizer & General Electronics Repair",
            nameBn = "স্ট্যাবিলাইজার ও অন্যান্য ইলেকট্রনিক্স মেরামত",
            descriptionEn = "Repair for voltage stabilizers, extension boards, fans, and household electronic devices.",
            descriptionBn = "ভোল্টেজ স্ট্যাবিলাইজার, এক্সটেনশন বোর্ড ও বাসার অন্যান্য ইলেকট্রনিক্স মেরামত।",
            featuresEn = listOf("Circuit board diagnostics", "Relay & transformer check", "Safe insulation testing"),
            featuresBn = listOf("সার্কিট বোর্ড পরীক্ষা", "রিলে ও ট্রান্সফরমার চেক", "নিরাপদ ইনসুলেশন টেস্ট"),
            price = 250.0,
            priceType = "estimate",
            priceDisplayEn = "After device check",
            priceDisplayBn = "যন্ত্র পরীক্ষার পর নির্ধারিত",
            isPopular = false
        )
    )

    fun businessSettingsPayload(): Map<String, Any> = mapOf(
        "businessName" to "Yusuf Electronics",
        "taglineEn" to "Complete Electrical, AC, CCTV & Home Services",
        "taglineBn" to "সম্পূর্ণ ইলেকট্রিক্যাল, এসি, সিসিটিভি ও হোম সার্ভিস",
        "phoneNumber" to "",
        "whatsappNumber" to "",
        "email" to "",
        "addressEn" to "",
        "addressBn" to "",
        "mapsQueryUrl" to "",
        "workingHoursEn" to "9:00 AM - 9:00 PM (7 Days)",
        "workingHoursBn" to "সকাল ৯:০০ - রাত ৯:০০ (সপ্তাহে ৭ দিন)",
        "availabilityStatus" to "online",
        "serviceAreasEn" to "Dhaka & Surrounding Areas",
        "serviceAreasBn" to "ঢাকা ও পার্শ্ববর্তী এলাকা",
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun defaultCategoryModels(): List<ServiceCategory> =
        categories
            .map { cat ->
                ServiceCategory(
                    id = cat.id,
                    nameEn = cat.nameEn,
                    nameBn = cat.nameBn,
                    descriptionEn = cat.descriptionEn,
                    descriptionBn = cat.descriptionBn,
                    iconKey = cat.iconKey,
                    status = "active",
                    sortOrder = cat.sortOrder
                )
            }
            .sortedBy { it.sortOrder }

    fun defaultServiceModels(): List<ServiceItem> =
        services
            .map { svc ->
                ServiceItem(
                    id = svc.id,
                    categoryId = svc.categoryId,
                    nameEn = svc.nameEn,
                    nameBn = svc.nameBn,
                    descriptionEn = svc.descriptionEn,
                    descriptionBn = svc.descriptionBn,
                    featuresEn = svc.featuresEn,
                    featuresBn = svc.featuresBn,
                    price = svc.price,
                    priceType = svc.priceType,
                    priceDisplayEn = svc.priceDisplayEn,
                    priceDisplayBn = svc.priceDisplayBn,
                    availability = "available",
                    isPopular = svc.isPopular
                )
            }
            .sortedBy { it.nameEn }
}

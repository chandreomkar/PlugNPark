package com.example.plugnpark.data.local.db

import androidx.room.withTransaction
import com.example.plugnpark.data.local.entity.AppNotification
import com.example.plugnpark.data.local.entity.AvailabilityRule
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.data.local.entity.ChargerSpec
import com.example.plugnpark.data.local.entity.Favorite
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.ListingPhoto
import com.example.plugnpark.data.local.entity.ParkingSpec
import com.example.plugnpark.data.local.entity.Review
import com.example.plugnpark.data.local.entity.User
import com.example.plugnpark.data.local.entity.Vehicle
import com.example.plugnpark.domain.model.BookingStatus
import com.example.plugnpark.domain.model.ListingType

object DatabaseInitializer {

    suspend fun seedDatabase(db: PlugNParkDatabase) {
        // Run once check
        if (db.userDao().getUserCount() > 0 || db.listingDao().getListingCount() > 0) return

        db.withTransaction {
            // 1. 6 USERS (1 Seeker, 5 Hosts)
            val users = listOf(
                User(
                    id = "user_seeker_1",
                    name = "Rahul Sharma",
                    email = "rahul.sharma@example.com",
                    phone = "+91 98765 43210",
                    avatarUrl = "https://picsum.photos/seed/user1/200",
                    role = "SEEKER",
                    walletBalancePaise = 250000L
                ),
                User(
                    id = "host_mumbai_1",
                    name = "Vikramaditya Mehta",
                    email = "vikram.m@example.com",
                    phone = "+91 98200 11223",
                    avatarUrl = "https://picsum.photos/seed/host1/200",
                    role = "HOST",
                    walletBalancePaise = 850000L
                ),
                User(
                    id = "host_mumbai_2",
                    name = "Ananya Deshmukh",
                    email = "ananya.d@example.com",
                    phone = "+91 98333 44556",
                    avatarUrl = "https://picsum.photos/seed/host2/200",
                    role = "HOST",
                    walletBalancePaise = 620000L
                ),
                User(
                    id = "host_mumbai_3",
                    name = "Rajesh Singhania",
                    email = "rajesh.s@example.com",
                    phone = "+91 98111 22334",
                    avatarUrl = "https://picsum.photos/seed/host3/200",
                    role = "HOST",
                    walletBalancePaise = 410000L
                ),
                User(
                    id = "host_pune_1",
                    name = "Kedar Kulkarni",
                    email = "kedar.k@example.com",
                    phone = "+91 97654 32109",
                    avatarUrl = "https://picsum.photos/seed/host4/200",
                    role = "HOST",
                    walletBalancePaise = 540000L
                ),
                User(
                    id = "host_pune_2",
                    name = "Priya Joshi",
                    email = "priya.j@example.com",
                    phone = "+91 97555 66778",
                    avatarUrl = "https://picsum.photos/seed/host5/200",
                    role = "HOST",
                    walletBalancePaise = 390000L
                )
            )
            db.userDao().insertUsers(users)

            // 2. 3 VEHICLES FOR SEEKER
            val vehicles = listOf(
                Vehicle(
                    id = "veh_1",
                    ownerId = "user_seeker_1",
                    make = "Tata",
                    model = "Nexon EV Empowered+",
                    vehicleType = "SUV",
                    isEv = true,
                    connectorType = "CCS2",
                    batteryKwh = 40.5,
                    plateNumber = "MH 02 FZ 8899",
                    isDefault = true
                ),
                Vehicle(
                    id = "veh_2",
                    ownerId = "user_seeker_1",
                    make = "Ather",
                    model = "450X Gen 3",
                    vehicleType = "BIKE",
                    isEv = true,
                    connectorType = "15A 3-Pin",
                    batteryKwh = 3.7,
                    plateNumber = "MH 02 EQ 1234"
                ),
                Vehicle(
                    id = "veh_3",
                    ownerId = "user_seeker_1",
                    make = "Hyundai",
                    model = "Creta SX",
                    vehicleType = "SUV",
                    isEv = false,
                    connectorType = "None",
                    batteryKwh = 0.0,
                    plateNumber = "MH 02 CD 5678"
                )
            )
            db.vehicleDao().insertVehicles(vehicles)

            // 3. 20 REALISTIC LISTINGS (10 EV Chargers, 10 Parking)
            val rawListingsData = listOf(
                // MUMBAI - Bandra
                RawListingData(
                    id = "list_1", type = ListingType.EV_CHARGER, title = "Fast 22kW AC Charger at Hill Road Villa",
                    description = "Gated private villa parking with Type 2 22kW AC fast charger. Shaded parking spot with 24/7 CCTV surveillance.",
                    address = "Hill Road, Bandra West, Mumbai, Maharashtra 400050", city = "Mumbai", area = "Bandra",
                    lat = 19.0544, lng = 72.8318, pricePerHour = 180.0, rating = 4.9f, ratingCount = 24, isVerified = true,
                    hostId = "host_mumbai_1", connectorType = "Type 2", powerKw = 22.0, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler", is24x7 = true,
                    images = listOf("https://images.unsplash.com/photo-1563720223185-11003d516935", "https://images.unsplash.com/photo-1558441719-670b95752402")
                ),
                RawListingData(
                    id = "list_2", type = ListingType.PARKING, title = "Spacious Covered Parking near BKC Financial Hub",
                    description = "5 minutes drive to BKC financial district. Dedicated underground spot with security guard.",
                    address = "Near Asian Heart Institute, BKC, Bandra East, Mumbai 400051", city = "Mumbai", area = "Bandra",
                    lat = 19.0657, lng = 72.8680, pricePerHour = 100.0, rating = 4.7f, ratingCount = 38, isVerified = true,
                    hostId = "host_mumbai_1", connectorType = "None", powerKw = 0.0, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler / SUV", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1506521781263-d8422e82f27a", "https://images.unsplash.com/photo-1590674899484-d5640e854abe")
                ),
                RawListingData(
                    id = "list_3", type = ListingType.PARKING, title = "Prime Stilt Parking near Carter Road Promenade",
                    description = "Secure covered spot ideal for seaside visits, cafes, or evening walks in Bandra.",
                    address = "Carter Road, Bandra West, Mumbai 400050", city = "Mumbai", area = "Bandra",
                    lat = 19.0600, lng = 72.8220, pricePerHour = 120.0, rating = 4.8f, ratingCount = 19, isVerified = true,
                    hostId = "host_mumbai_1", connectorType = "None", powerKw = 0.0, isCovered = true, hasCCTV = false,
                    vehicleType = "4-Wheeler", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1590674899484-d5640e854abe", "https://images.unsplash.com/photo-1506521781263-d8422e82f27a")
                ),
                RawListingData(
                    id = "list_4", type = ListingType.EV_CHARGER, title = "BKC Tech Park Office EV Wallbox 7.4kW",
                    description = "Located 2 minutes from BKC corporate towers. Office parking spot available after hours and weekends.",
                    address = "Kalanagar, Bandra East, Mumbai 400051", city = "Mumbai", area = "Bandra",
                    lat = 19.0610, lng = 72.8520, pricePerHour = 160.0, rating = 4.6f, ratingCount = 15, isVerified = true,
                    hostId = "host_mumbai_1", connectorType = "Type 2", powerKw = 7.4, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler", isOfficeAfter6pm = true,
                    images = listOf("https://images.unsplash.com/photo-1558441719-670b95752402", "https://images.unsplash.com/photo-1563720223185-11003d516935")
                ),

                // MUMBAI - Andheri
                RawListingData(
                    id = "list_5", type = ListingType.EV_CHARGER, title = "7.4kW Smart Wallbox in Lokhandwala",
                    description = "Residential apartment slot with 7.4kW single-phase fast charger. Compatible with Tata Nexon EV, MG ZS EV.",
                    address = "Lokhandwala Complex, Andheri West, Mumbai 400053", city = "Mumbai", area = "Andheri",
                    lat = 19.1412, lng = 72.8256, pricePerHour = 140.0, rating = 4.8f, ratingCount = 31, isVerified = true,
                    hostId = "host_mumbai_2", connectorType = "CCS2 / Type 2", powerKw = 7.4, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler", is24x7 = true,
                    images = listOf("https://images.unsplash.com/photo-1563720223185-11003d516935", "https://images.unsplash.com/photo-1558441719-670b95752402")
                ),
                RawListingData(
                    id = "list_6", type = ListingType.PARKING, title = "Safe SUV Parking Spot near JB Nagar Metro",
                    description = "Convenient parking spot right opposite JB Nagar metro station. Quick exit to Western Express Highway.",
                    address = "JB Nagar, Andheri East, Mumbai 400059", city = "Mumbai", area = "Andheri",
                    lat = 19.1120, lng = 72.8690, pricePerHour = 60.0, rating = 4.5f, ratingCount = 22, isVerified = false,
                    hostId = "host_mumbai_2", connectorType = "None", powerKw = 0.0, isCovered = false, hasCCTV = true,
                    vehicleType = "4-Wheeler / SUV", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1506521781263-d8422e82f27a", "https://images.unsplash.com/photo-1590674899484-d5640e854abe")
                ),
                RawListingData(
                    id = "list_7", type = ListingType.EV_CHARGER, title = "15A 3-Pin Socket for 2W & 4W EV in Versova",
                    description = "Budget friendly home charger with portable cable access. Ideal for Ola S1, Ather 450X, or slow car charge.",
                    address = "Versova, Andheri West, Mumbai 400061", city = "Mumbai", area = "Andheri",
                    lat = 19.1350, lng = 72.8140, pricePerHour = 90.0, rating = 4.4f, ratingCount = 11, isVerified = true,
                    hostId = "host_mumbai_2", connectorType = "15A 3-Pin", powerKw = 3.3, isCovered = true, hasCCTV = true,
                    vehicleType = "2-Wheeler / 4-Wheeler", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1558441719-670b95752402", "https://images.unsplash.com/photo-1563720223185-11003d516935")
                ),
                RawListingData(
                    id = "list_8", type = ListingType.PARKING, title = "Gated Courtyard Parking - Veera Desai Road",
                    description = "Quiet private driveway spot near Fun Republic theatre. Excellent security and wide turning space.",
                    address = "Veera Desai Road, Andheri West, Mumbai 400053", city = "Mumbai", area = "Andheri",
                    lat = 19.1380, lng = 72.8330, pricePerHour = 70.0, rating = 4.7f, ratingCount = 14, isVerified = true,
                    hostId = "host_mumbai_2", connectorType = "None", powerKw = 0.0, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1590674899484-d5640e854abe", "https://images.unsplash.com/photo-1506521781263-d8422e82f27a")
                ),

                // MUMBAI - Powai
                RawListingData(
                    id = "list_9", type = ListingType.EV_CHARGER, title = "Hiranandani Gardens Fast EV Point",
                    description = "High-power 11kW AC charger in central Hiranandani. Relax at nearby cafes while your vehicle charges.",
                    address = "Central Avenue, Hiranandani Gardens, Powai, Mumbai 400076", city = "Mumbai", area = "Powai",
                    lat = 19.1197, lng = 72.9051, pricePerHour = 200.0, rating = 4.9f, ratingCount = 42, isVerified = true,
                    hostId = "host_mumbai_3", connectorType = "Type 2", powerKw = 11.0, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler", is24x7 = true,
                    images = listOf("https://images.unsplash.com/photo-1563720223185-11003d516935", "https://images.unsplash.com/photo-1558441719-670b95752402", "https://images.unsplash.com/photo-1506521781263-d8422e82f27a")
                ),
                RawListingData(
                    id = "list_10", type = ListingType.PARKING, title = "Gated Apartment Spot - Powai Lake View",
                    description = "Covered stilt parking in premium society near Powai lake promenade. CCTV monitoring and watchman on duty.",
                    address = "Saki Vihar Road, Powai, Mumbai 400072", city = "Mumbai", area = "Powai",
                    lat = 19.1280, lng = 72.8990, pricePerHour = 50.0, rating = 4.6f, ratingCount = 18, isVerified = true,
                    hostId = "host_mumbai_3", connectorType = "None", powerKw = 0.0, isCovered = true, hasCCTV = true,
                    vehicleType = "2-Wheeler / 4-Wheeler", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1506521781263-d8422e82f27a", "https://images.unsplash.com/photo-1590674899484-d5640e854abe")
                ),
                RawListingData(
                    id = "list_11", type = ListingType.EV_CHARGER, title = "IIT Bombay Gate 2 Fast Charger Hub",
                    description = "CCS2 22kW Fast Charger installed in private bungalow driveway. Auto-shutoff safety breakers installed.",
                    address = "Main Gate Rd, IIT Area, Powai, Mumbai 400076", city = "Mumbai", area = "Powai",
                    lat = 19.1310, lng = 72.9150, pricePerHour = 220.0, rating = 4.8f, ratingCount = 29, isVerified = true,
                    hostId = "host_mumbai_3", connectorType = "CCS2", powerKw = 22.0, isCovered = false, hasCCTV = true,
                    vehicleType = "4-Wheeler", is24x7 = true,
                    images = listOf("https://images.unsplash.com/photo-1558441719-670b95752402", "https://images.unsplash.com/photo-1563720223185-11003d516935")
                ),

                // PUNE - Wakad
                RawListingData(
                    id = "list_12", type = ListingType.EV_CHARGER, title = "Datta Mandir Road 7.4kW EV Charging Point",
                    description = "Easy drive-in spot in residential row house. Clean, wide space suitable for Tata Nexon & Mahindra XUV400.",
                    address = "Datta Mandir Road, Wakad, Pune, Maharashtra 411057", city = "Pune", area = "Wakad",
                    lat = 18.5987, lng = 73.7645, pricePerHour = 130.0, rating = 4.7f, ratingCount = 20, isVerified = true,
                    hostId = "host_pune_1", connectorType = "Type 2", powerKw = 7.4, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler / SUV", is24x7 = true,
                    images = listOf("https://images.unsplash.com/photo-1563720223185-11003d516935", "https://images.unsplash.com/photo-1558441719-670b95752402")
                ),
                RawListingData(
                    id = "list_13", type = ListingType.PARKING, title = "Budget Car & Bike Spot near Bhumkar Chowk",
                    description = "Open shaded drive plot near Mumbai-Bangalore highway exit. Ideal for Hinjewadi commuters.",
                    address = "Bhumkar Chowk, Wakad, Pune 411057", city = "Pune", area = "Wakad",
                    lat = 18.6010, lng = 73.7530, pricePerHour = 35.0, rating = 4.3f, ratingCount = 16, isVerified = false,
                    hostId = "host_pune_1", connectorType = "None", powerKw = 0.0, isCovered = false, hasCCTV = true,
                    vehicleType = "2-Wheeler / 4-Wheeler", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1506521781263-d8422e82f27a", "https://images.unsplash.com/photo-1590674899484-d5640e854abe")
                ),
                RawListingData(
                    id = "list_14", type = ListingType.PARKING, title = "Covered Society Spot near Phoenix Mall Wakad",
                    description = "10 mins walk from Phoenix Marketcity Wakad. Avoid high mall parking fees and long queues!",
                    address = "Kaspate Wasti, Wakad, Pune 411057", city = "Pune", area = "Wakad",
                    lat = 18.5940, lng = 73.7710, pricePerHour = 40.0, rating = 4.8f, ratingCount = 33, isVerified = true,
                    hostId = "host_pune_1", connectorType = "None", powerKw = 0.0, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1590674899484-d5640e854abe", "https://images.unsplash.com/photo-1506521781263-d8422e82f27a")
                ),

                // PUNE - Hinjewadi
                RawListingData(
                    id = "list_15", type = ListingType.EV_CHARGER, title = "Phase 1 Tech Park Fast Charger 11kW",
                    description = "Located right next to Rajiv Gandhi Infotech Park Phase 1. Available after 6 pm on weekdays & 24 hrs on weekends.",
                    address = "Phase 1, Hinjewadi Rajiv Gandhi Infotech Park, Pune 411057", city = "Pune", area = "Hinjewadi",
                    lat = 18.5912, lng = 73.7380, pricePerHour = 175.0, rating = 4.8f, ratingCount = 50, isVerified = true,
                    hostId = "host_pune_2", connectorType = "Type 2 / CCS2", powerKw = 11.0, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler", isOfficeAfter6pm = true,
                    images = listOf("https://images.unsplash.com/photo-1558441719-670b95752402", "https://images.unsplash.com/photo-1563720223185-11003d516935")
                ),
                RawListingData(
                    id = "list_16", type = ListingType.PARKING, title = "Secure Stilt Car Parking near IT Park Phase 2",
                    description = "Covered parking slot in modern gated society with RFID boom barrier entry.",
                    address = "Phase 2, Hinjewadi, Pune 411057", city = "Pune", area = "Hinjewadi",
                    lat = 18.5810, lng = 73.7190, pricePerHour = 45.0, rating = 4.6f, ratingCount = 27, isVerified = true,
                    hostId = "host_pune_2", connectorType = "None", powerKw = 0.0, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1506521781263-d8422e82f27a", "https://images.unsplash.com/photo-1590674899484-d5640e854abe")
                ),
                RawListingData(
                    id = "list_17", type = ListingType.EV_CHARGER, title = "22kW Dual Port Heavy Charger - Hinjewadi Phase 3",
                    description = "Commercial grade AC charger setup in private office campus parking.",
                    address = "Phase 3, Hinjewadi, Pune 411057", city = "Pune", area = "Hinjewadi",
                    lat = 18.5710, lng = 73.6980, pricePerHour = 240.0, rating = 4.9f, ratingCount = 14, isVerified = true,
                    hostId = "host_pune_2", connectorType = "Type 2", powerKw = 22.0, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler", is24x7 = true,
                    images = listOf("https://images.unsplash.com/photo-1563720223185-11003d516935", "https://images.unsplash.com/photo-1558441719-670b95752402")
                ),

                // PUNE - Kothrud
                RawListingData(
                    id = "list_18", type = ListingType.EV_CHARGER, title = "Karve Road 15A Home Socket for Two-Wheelers",
                    description = "Residential bungalow porch with heavy-duty 15A socket. Refreshment water & tea available on request!",
                    address = "Karve Road, Kothrud, Pune 411038", city = "Pune", area = "Kothrud",
                    lat = 18.5074, lng = 73.8077, pricePerHour = 85.0, rating = 4.5f, ratingCount = 17, isVerified = true,
                    hostId = "host_pune_2", connectorType = "15A 3-Pin", powerKw = 3.3, isCovered = true, hasCCTV = false,
                    vehicleType = "2-Wheeler / 4-Wheeler", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1558441719-670b95752402", "https://images.unsplash.com/photo-1563720223185-11003d516935")
                ),
                RawListingData(
                    id = "list_19", type = ListingType.PARKING, title = "Chandani Chowk Covered SUV Spot",
                    description = "Wide driveway spot close to Pune-Satara highway bypass exit. Very easy parking for large SUVs.",
                    address = "Near Chandani Chowk, Kothrud, Pune 411038", city = "Pune", area = "Kothrud",
                    lat = 18.5030, lng = 73.7850, pricePerHour = 50.0, rating = 4.7f, ratingCount = 21, isVerified = true,
                    hostId = "host_pune_2", connectorType = "None", powerKw = 0.0, isCovered = true, hasCCTV = true,
                    vehicleType = "SUV / 4-Wheeler", is24x7 = false,
                    images = listOf("https://images.unsplash.com/photo-1506521781263-d8422e82f27a", "https://images.unsplash.com/photo-1590674899484-d5640e854abe")
                ),
                RawListingData(
                    id = "list_20", type = ListingType.EV_CHARGER, title = "Ideal Colony 7.4kW Solar Smart Wallbox",
                    description = "Residential charger point with solar panel back-up during daytime hours. Eco-friendly charging!",
                    address = "Ideal Colony, Kothrud, Pune 411038", city = "Pune", area = "Kothrud",
                    lat = 18.5120, lng = 73.8150, pricePerHour = 150.0, rating = 4.9f, ratingCount = 36, isVerified = true,
                    hostId = "host_pune_2", connectorType = "Type 2", powerKw = 7.4, isCovered = true, hasCCTV = true,
                    vehicleType = "4-Wheeler", is24x7 = true,
                    images = listOf("https://images.unsplash.com/photo-1563720223185-11003d516935", "https://images.unsplash.com/photo-1558441719-670b95752402")
                )
            )

            val listingsToInsert = mutableListOf<Listing>()
            val chargerSpecsToInsert = mutableListOf<ChargerSpec>()
            val parkingSpecsToInsert = mutableListOf<ParkingSpec>()
            val photosToInsert = mutableListOf<ListingPhoto>()
            val availabilityRulesToInsert = mutableListOf<AvailabilityRule>()

            rawListingsData.forEach { data ->
                val listing = Listing(
                    id = data.id,
                    hostId = data.hostId,
                    type = data.type,
                    title = data.title,
                    description = data.description,
                    address = data.address,
                    city = data.city,
                    area = data.area,
                    lat = data.lat,
                    lng = data.lng,
                    pricePerHourPaise = (data.pricePerHour * 100).toLong(),
                    isVerified = data.isVerified,
                    isActive = true,
                    instantBook = true,
                    avgRating = data.rating,
                    ratingCount = data.ratingCount
                )
                listingsToInsert.add(listing)

                if (data.type == ListingType.EV_CHARGER) {
                    chargerSpecsToInsert.add(
                        ChargerSpec(
                            listingId = data.id,
                            connectorType = data.connectorType,
                            powerKw = data.powerKw,
                            hasEarthing = true,
                            hasMcb = true,
                            isWeatherproof = true
                        )
                    )
                } else {
                    parkingSpecsToInsert.add(
                        ParkingSpec(
                            listingId = data.id,
                            vehicleTypes = data.vehicleType,
                            isCovered = data.isCovered,
                            hasCctv = data.hasCCTV,
                            hasGuard = true,
                            totalSlots = 1,
                            accessNotes = "Gated access"
                        )
                    )
                }

                // Photos (2 to 3 per listing)
                data.images.forEachIndexed { imgIndex, url ->
                    photosToInsert.add(
                        ListingPhoto(
                            id = "photo_${data.id}_${imgIndex + 1}",
                            listingId = data.id,
                            url = url,
                            sortOrder = imgIndex
                        )
                    )
                }

                // Weekly Availability Rules
                if (data.is24x7) {
                    (1..7).forEach { day ->
                        availabilityRulesToInsert.add(
                            AvailabilityRule(
                                id = "rule_${data.id}_$day",
                                listingId = data.id,
                                dayOfWeek = day,
                                startMinute = 0,
                                endMinute = 1439
                            )
                        )
                    }
                } else if (data.isOfficeAfter6pm) {
                    (1..5).forEach { day ->
                        availabilityRulesToInsert.add(
                            AvailabilityRule(
                                id = "rule_${data.id}_$day",
                                listingId = data.id,
                                dayOfWeek = day,
                                startMinute = 1080, // 18:00
                                endMinute = 1439    // 23:59
                            )
                        )
                    }
                    (6..7).forEach { day ->
                        availabilityRulesToInsert.add(
                            AvailabilityRule(
                                id = "rule_${data.id}_$day",
                                listingId = data.id,
                                dayOfWeek = day,
                                startMinute = 0,
                                endMinute = 1439
                            )
                        )
                    }
                } else {
                    (1..7).forEach { day ->
                        availabilityRulesToInsert.add(
                            AvailabilityRule(
                                id = "rule_${data.id}_$day",
                                listingId = data.id,
                                dayOfWeek = day,
                                startMinute = 360,  // 06:00
                                endMinute = 1320   // 22:00
                            )
                        )
                    }
                }
            }

            db.listingDao().insertListings(listingsToInsert)
            db.chargerSpecDao().insertChargerSpecs(chargerSpecsToInsert)
            db.parkingSpecDao().insertParkingSpecs(parkingSpecsToInsert)
            db.listingPhotoDao().insertPhotos(photosToInsert)
            db.availabilityRuleDao().insertRules(availabilityRulesToInsert)

            // 4. 8 BOOKINGS (Past and Upcoming across statuses)
            val now = System.currentTimeMillis()
            val oneHour = 3600_000L
            val oneDay = 86400_000L

            val sampleBookings = listOf(
                Booking(
                    id = "book_1", listingId = "list_1", seekerId = "user_seeker_1", vehicleId = "veh_1",
                    startTime = now - (2 * oneDay), endTime = now - (2 * oneDay) + (2 * oneHour),
                    baseAmountPaise = 36000L, platformFeePaise = 3600L, gstPaise = 648L, totalPaise = 40248L, hostEarningPaise = 36000L,
                    status = BookingStatus.COMPLETED, paymentMethod = "UPI", paymentStatus = "COMPLETED", createdAt = now - (3 * oneDay)
                ),
                Booking(
                    id = "book_2", listingId = "list_2", seekerId = "user_seeker_1", vehicleId = "veh_3",
                    startTime = now - (1 * oneDay), endTime = now - (1 * oneDay) + (3 * oneHour),
                    baseAmountPaise = 30000L, platformFeePaise = 3000L, gstPaise = 540L, totalPaise = 33540L, hostEarningPaise = 30000L,
                    status = BookingStatus.COMPLETED, paymentMethod = "CARD", paymentStatus = "COMPLETED", createdAt = now - (2 * oneDay)
                ),
                Booking(
                    id = "book_3", listingId = "list_5", seekerId = "user_seeker_1", vehicleId = "veh_1",
                    startTime = now + (1 * oneDay), endTime = now + (1 * oneDay) + (2 * oneHour),
                    baseAmountPaise = 28000L, platformFeePaise = 2800L, gstPaise = 504L, totalPaise = 31304L, hostEarningPaise = 28000L,
                    status = BookingStatus.CONFIRMED, paymentMethod = "UPI", paymentStatus = "COMPLETED", createdAt = now - (4 * 3600_000L)
                ),
                Booking(
                    id = "book_4", listingId = "list_9", seekerId = "user_seeker_1", vehicleId = "veh_1",
                    startTime = now - (3 * oneDay), endTime = now - (3 * oneDay) + (2 * oneHour),
                    baseAmountPaise = 40000L, platformFeePaise = 4000L, gstPaise = 720L, totalPaise = 44720L, hostEarningPaise = 40000L,
                    status = BookingStatus.COMPLETED, paymentMethod = "UPI", paymentStatus = "COMPLETED", createdAt = now - (4 * oneDay)
                ),
                Booking(
                    id = "book_5", listingId = "list_12", seekerId = "user_seeker_1", vehicleId = "veh_1",
                    startTime = now - (5 * oneDay), endTime = now - (5 * oneDay) + (3 * oneHour),
                    baseAmountPaise = 39000L, platformFeePaise = 3900L, gstPaise = 702L, totalPaise = 43602L, hostEarningPaise = 39000L,
                    status = BookingStatus.CANCELLED, paymentMethod = "UPI", paymentStatus = "REFUNDED", createdAt = now - (6 * oneDay),
                    cancelledAt = now - (5 * oneDay) - (2 * oneHour), refundPaise = 43602L
                ),
                Booking(
                    id = "book_6", listingId = "list_15", seekerId = "user_seeker_1", vehicleId = "veh_1",
                    startTime = now + (2 * oneDay), endTime = now + (2 * oneDay) + (4 * oneHour),
                    baseAmountPaise = 70000L, platformFeePaise = 7000L, gstPaise = 1260L, totalPaise = 78260L, hostEarningPaise = 70000L,
                    status = BookingStatus.CONFIRMED, paymentMethod = "CARD", paymentStatus = "COMPLETED", createdAt = now - (12 * 3600_000L)
                ),
                Booking(
                    id = "book_7", listingId = "list_18", seekerId = "user_seeker_1", vehicleId = "veh_2",
                    startTime = now - (4 * oneDay), endTime = now - (4 * oneDay) + (2 * oneHour),
                    baseAmountPaise = 17000L, platformFeePaise = 1700L, gstPaise = 306L, totalPaise = 19006L, hostEarningPaise = 17000L,
                    status = BookingStatus.COMPLETED, paymentMethod = "WALLET", paymentStatus = "COMPLETED", createdAt = now - (5 * oneDay)
                ),
                Booking(
                    id = "book_8", listingId = "list_20", seekerId = "user_seeker_1", vehicleId = "veh_1",
                    startTime = now + (3 * oneDay), endTime = now + (3 * oneDay) + (3 * oneHour),
                    baseAmountPaise = 45000L, platformFeePaise = 4500L, gstPaise = 810L, totalPaise = 50310L, hostEarningPaise = 45000L,
                    status = BookingStatus.CONFIRMED, paymentMethod = "UPI", paymentStatus = "COMPLETED", createdAt = now - (2 * 3600_000L)
                ),
                Booking(id = "book_dummy_5", listingId = "list_3", seekerId = "user_seeker_1", vehicleId = "veh_3", startTime = now - (8 * oneDay), endTime = now - (8 * oneDay) + (2 * oneHour), baseAmountPaise = 24000L, platformFeePaise = 2400L, gstPaise = 432L, totalPaise = 26832L, hostEarningPaise = 24000L, status = BookingStatus.COMPLETED, paymentMethod = "UPI", paymentStatus = "COMPLETED", createdAt = now - (9 * oneDay)),
                Booking(id = "book_dummy_6", listingId = "list_7", seekerId = "user_seeker_1", vehicleId = "veh_2", startTime = now - (10 * oneDay), endTime = now - (10 * oneDay) + (2 * oneHour), baseAmountPaise = 18000L, platformFeePaise = 1800L, gstPaise = 324L, totalPaise = 20124L, hostEarningPaise = 18000L, status = BookingStatus.COMPLETED, paymentMethod = "UPI", paymentStatus = "COMPLETED", createdAt = now - (11 * oneDay)),
                Booking(id = "book_dummy_7", listingId = "list_11", seekerId = "user_seeker_1", vehicleId = "veh_1", startTime = now - (12 * oneDay), endTime = now - (12 * oneDay) + (2 * oneHour), baseAmountPaise = 44000L, platformFeePaise = 4400L, gstPaise = 792L, totalPaise = 49192L, hostEarningPaise = 44000L, status = BookingStatus.COMPLETED, paymentMethod = "UPI", paymentStatus = "COMPLETED", createdAt = now - (13 * oneDay)),
                Booking(id = "book_dummy_8", listingId = "list_14", seekerId = "user_seeker_1", vehicleId = "veh_3", startTime = now - (14 * oneDay), endTime = now - (14 * oneDay) + (3 * oneHour), baseAmountPaise = 12000L, platformFeePaise = 1200L, gstPaise = 216L, totalPaise = 13416L, hostEarningPaise = 12000L, status = BookingStatus.COMPLETED, paymentMethod = "CARD", paymentStatus = "COMPLETED", createdAt = now - (15 * oneDay)),
                Booking(id = "book_dummy_9", listingId = "list_17", seekerId = "user_seeker_1", vehicleId = "veh_1", startTime = now - (15 * oneDay), endTime = now - (15 * oneDay) + (2 * oneHour), baseAmountPaise = 48000L, platformFeePaise = 4800L, gstPaise = 864L, totalPaise = 53664L, hostEarningPaise = 48000L, status = BookingStatus.COMPLETED, paymentMethod = "UPI", paymentStatus = "COMPLETED", createdAt = now - (16 * oneDay)),
                Booking(id = "book_dummy_10", listingId = "list_19", seekerId = "user_seeker_1", vehicleId = "veh_3", startTime = now - (18 * oneDay), endTime = now - (18 * oneDay) + (2 * oneHour), baseAmountPaise = 10000L, platformFeePaise = 1000L, gstPaise = 180L, totalPaise = 11180L, hostEarningPaise = 10000L, status = BookingStatus.COMPLETED, paymentMethod = "UPI", paymentStatus = "COMPLETED", createdAt = now - (19 * oneDay))
            )
            db.bookingDao().insertBookings(sampleBookings)

            // 5. 10 REVIEWS
            val sampleReviews = listOf(
                Review(id = "rev_1", bookingId = "book_1", listingId = "list_1", reviewerId = "user_seeker_1", reviewerName = "Rahul Sharma", rating = 5.0f, comment = "Excellent fast charger! Host Vikramaditya was very helpful and showed me where to park.", createdAt = now - (2 * oneDay)),
                Review(id = "rev_2", bookingId = "book_2", listingId = "list_2", reviewerId = "user_seeker_1", reviewerName = "Rahul Sharma", rating = 4.8f, comment = "Very clean parking spot near BKC. Saved me a lot of hassle during office hours.", createdAt = now - (1 * oneDay)),
                Review(id = "rev_3", bookingId = "book_4", listingId = "list_9", reviewerId = "user_seeker_1", reviewerName = "Rahul Sharma", rating = 5.0f, comment = "Hiranandani Powai spot is superb. Charged my Nexon EV while enjoying coffee nearby.", createdAt = now - (3 * oneDay)),
                Review(id = "rev_4", bookingId = "book_7", listingId = "list_18", reviewerId = "user_seeker_1", reviewerName = "Rahul Sharma", rating = 4.7f, comment = "Perfect 15A socket for my Ather 450X. Quiet bungalow porch in Kothrud.", createdAt = now - (4 * oneDay)),
                Review(id = "rev_5", bookingId = "book_dummy_5", listingId = "list_3", reviewerId = "user_seeker_1", reviewerName = "Priya Deshmukh", rating = 4.9f, comment = "Very peaceful Carter Road parking spot. Gated courtyard.", createdAt = now - (8 * oneDay)),
                Review(id = "rev_6", bookingId = "book_dummy_6", listingId = "list_7", reviewerId = "user_seeker_1", reviewerName = "Amit Patel", rating = 4.5f, comment = "Good Versova socket. Very cooperative host.", createdAt = now - (10 * oneDay)),
                Review(id = "rev_7", bookingId = "book_dummy_7", listingId = "list_11", reviewerId = "user_seeker_1", reviewerName = "Siddharth Joshi", rating = 5.0f, comment = "Superfast 22kW charging right outside IIT Powai gate.", createdAt = now - (12 * oneDay)),
                Review(id = "rev_8", bookingId = "book_dummy_8", listingId = "list_14", reviewerId = "user_seeker_1", reviewerName = "Sneha Kulkarni", rating = 4.8f, comment = "Avoided high Phoenix Wakad mall parking fees! Covered and safe.", createdAt = now - (14 * oneDay)),
                Review(id = "rev_9", bookingId = "book_dummy_9", listingId = "list_17", reviewerId = "user_seeker_1", reviewerName = "Karan Verma", rating = 4.9f, comment = "Hinjewadi Phase 3 heavy charger worked flawlessly.", createdAt = now - (15 * oneDay)),
                Review(id = "rev_10", bookingId = "book_dummy_10", listingId = "list_19", reviewerId = "user_seeker_1", reviewerName = "Gaurav Roy", rating = 4.7f, comment = "Wide driveway for SUV near Chandani Chowk exit.", createdAt = now - (18 * oneDay))
            )
            db.reviewDao().insertReviews(sampleReviews)

            // 6. 3 FAVORITES FOR SEEKER
            db.favoriteDao().insertFavorite(Favorite("user_seeker_1", "list_1"))
            db.favoriteDao().insertFavorite(Favorite("user_seeker_1", "list_5"))
            db.favoriteDao().insertFavorite(Favorite("user_seeker_1", "list_9"))

            // 7. 5 APP NOTIFICATIONS FOR SEEKER
            val notifications = listOf(
                AppNotification(id = "notif_1", userId = "user_seeker_1", title = "Booking Confirmed! 🎉", body = "Your slot at Hill Road Villa Charger is reserved for tomorrow.", type = "BOOKING", createdAt = now - (4 * 3600_000L)),
                AppNotification(id = "notif_2", userId = "user_seeker_1", title = "Charging Completed ⚡", body = "Your Nexon EV charged 24 kWh. Receipt available in Bookings.", type = "CHARGING", createdAt = now - (1 * oneDay)),
                AppNotification(id = "notif_3", userId = "user_seeker_1", title = "Refund Processed 💰", body = "Full refund of ₹436 processed for your cancelled Wakad booking.", type = "REFUND", createdAt = now - (5 * oneDay)),
                AppNotification(id = "notif_4", userId = "user_seeker_1", title = "New Fast Charger Near You! ⚡", body = "A new 22kW CCS2 charger added in Bandra West.", type = "PROMO", createdAt = now - (7 * oneDay)),
                AppNotification(id = "notif_5", userId = "user_seeker_1", title = "Welcome to PlugNPark! 🚀", body = "Explore over 20 verified EV chargers and parking spots across Mumbai & Pune.", type = "WELCOME", createdAt = now - (10 * oneDay))
            )
            notifications.forEach { db.appNotificationDao().insertNotification(it) }
        }
    }

    private data class RawListingData(
        val id: String,
        val type: ListingType,
        val title: String,
        val description: String,
        val address: String,
        val city: String,
        val area: String,
        val lat: Double,
        val lng: Double,
        val pricePerHour: Double,
        val rating: Float,
        val ratingCount: Int,
        val isVerified: Boolean,
        val hostId: String,
        val connectorType: String,
        val powerKw: Double,
        val isCovered: Boolean,
        val hasCCTV: Boolean,
        val vehicleType: String,
        val is24x7: Boolean = false,
        val isOfficeAfter6pm: Boolean = false,
        val images: List<String>
    )
}

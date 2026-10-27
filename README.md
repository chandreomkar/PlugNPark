# PlugNPark ⚡🅿️ - Peer-to-Peer EV Charging & Parking Marketplace

**PlugNPark** is a native Android application built using Kotlin and Jetpack Compose (Material 3). It implements a peer-to-peer EV charger and parking spot marketplace tailored for Indian megacities (Mumbai, Pune, Bandra, Wakad, Powai, Hinjewadi, Andheri, Kothrud).

PlugNPark unlocks idle private driveway chargers and parking spots, allowing hosts to monetize their chargers and drivers to easily find, filter, navigate to, and reserve EV chargers or parking spots.

---

## 🛠 Tech Stack & Architecture

- **Language & Framework**: Kotlin 2.1, Android SDK 35 (`minSdk` 26)
- **UI Toolkit**: Jetpack Compose (Material 3, Dark & Light Mode Support, Material Icons Extended)
- **Local Database**: Room DB 2.8 with KSP (`exportSchema = true`, `fallbackToDestructiveMigration()`)
- **Architecture**: Unidirectional Data Flow (UDF), Repository Pattern, `ViewModel` & Compose `StateFlow`
- **Session Management**: Android Jetpack DataStore Preferences
- **Mapping & Location**: osmdroid OpenStreetMap, Google Play Services Location (`FusedLocationProviderClient`)
- **Background Tasks**: Android Jetpack WorkManager (`OneTimeWorkRequestBuilder` for scheduled start reminders)
- **Image Loading & QR Code**: Coil Compose, ZXing Core (QR Code Generation)

---

## 🗄 Database Schema & ER Diagram

The app runs on Room Database Version 2 containing 11 entities connected via Foreign Keys (`onDelete = CASCADE` / `SET_NULL`) and compound indices.

All monetary amounts are stored as `Long` in **paise** (1 Rupee = 100 paise) and times as `Long` epoch millis.

```text
 +------------------+           +------------------+           +------------------+
 |       User       |           |     Vehicle      |           |     Listing      |
 +------------------+           +------------------+           +------------------+
 | id (PK)          |<---------1| id (PK)          |     +---->| id (PK)          |
 | name             |           | ownerId (FK)     |     |     | hostId (FK)      |
 | phone            |           | make, model      |     |     | type, title      |
 | email, avatarUrl |           | vehicleType      |     |     | city, area       |
 | role             |           | isEv, connector  |     |     | lat, lng         |
 | walletBalance    |           | batteryKwh       |     |     | pricePerHourPaise|
 | createdAt        |           | plateNumber      |     |     | isVerified       |
 +------------------+           | isDefault        |     |     | isActive         |
          ^                     +------------------+     |     | instantBook      |
          |                                              |     | avgRating, count |
          |                     +------------------+     |     | createdAt        |
          +--------------------1|     Booking      |     |     +------------------+
          |                     +------------------+     |       ^   ^   ^   ^
          |                     | id (PK)          |     |       |   |   |   |
          |                     | listingId (FK)---+-----+       |   |   |   |
          +--------------------+| seekerId (FK)    |             |   |   |   |
          |                     | vehicleId (FK)   |             |   |   |   |
          |                     | startTime        |             |   |   |   |
          |                     | endTime          |             |   |   |   |
          |                     | totalPaise       |             |   |   |   |
          |                     | status           |             |   |   |   |
          |                     +------------------+             |   |   |   |
          |                              ^                       |   |   |   |
          |                              |                       |   |   |   |
          |                     +------------------+             |   |   |   |
          |                     |      Review      |             |   |   |   |
          |                     +------------------+             |   |   |   |
          |                     | id (PK)          |             |   |   |   |
          +--------------------+| bookingId (FK)   |             |   |   |   |
          |                     | listingId (FK)---+-------------+   |   |   |
          |                     | reviewerId (FK)  |                 |   |   |
          |                     | rating, comment  |                 |   |   |
          |                     +------------------+                 |   |   |
          |                                                          |   |   |
          |                     +------------------+                 |   |   |
          |                     |   ChargerSpec    |                 |   |   |
          |                     +------------------+                 |   |   |
          |                     | listingId(PK/FK)-+-----------------+   |   |
          |                     | connectorType    |                     |   |
          |                     | powerKw, mcb...  |                     |   |
          |                     +------------------+                     |   |
          |                                                              |   |
          |                     +------------------+                     |   |
          |                     |   ParkingSpec    |                     |   |
          |                     +------------------+                     |   |
          |                     | listingId(PK/FK)-+---------------------+   |
          |                     | vehicleTypes     |                         |
          |                     | isCovered, cctv  |                         |
          |                     +------------------+                         |
          |                                                                  |
          |                     +------------------+                         |
          |                     |   ListingPhoto   |                         |
          |                     +------------------+                         |
          |                     | id (PK)          |                         |
          |                     | listingId (FK)---+-------------------------+
          |                     | url, sortOrder   |
          |                     +------------------+
          |
          |                     +------------------+
          |                     | AvailabilityRule |
          |                     +------------------+
          |                     | id (PK)          |
          |                     | listingId (FK)---+-------------------------+
          |                     | dayOfWeek        |
          |                     | start/endMinute  |
          |                     +------------------+
          |
          |                     +------------------+
          |                     |     Favorite     |
          |                     +------------------+
          |                     | userId (PK/FK)   |
          |                     | listingId(PK/FK)-+-------------------------+
          |                     +------------------+
          |
          |                     +------------------+
          |                     | AppNotification  |
          |                     +------------------+
          |                     | id (PK)          |
          +--------------------+| userId (FK)      |
                                | title, body      |
                                | isRead           |
                                +------------------+
```

---

## 🚀 Step-by-Step Demo Walkthrough Script

### 1. Onboarding & OTP Login
- **Action**: Launch the app. Swipe through the 3 onboarding slides (*Find EV Chargers & Parking*, *Host & Earn Passive Income*, *Instant Navigation & Security*).
- **Action**: Tap **"Get Started"** to go to the Login screen.
- **Action**: Enter phone number `9876543210` and tap **"Send Verification Code"**.
- **Action**: Enter mock OTP `123456` and tap **"Verify & Proceed"**.
- **Action**: On the Role Selection screen, choose **"Both (Seeker & Host)"**.

### 2. Profile & Vehicle Management
- **Action**: Tap the **Profile** tab in the bottom bar.
- **Action**: Under **My Vehicles**, see the pre-populated Tata Nexon EV marked as `✔ Primary`.
- **Action**: Tap **"Add Vehicle"** to add a new EV scooter or car (e.g. Ather 450X, 15A 3-Pin).
- **Action**: Edit or set any vehicle as the primary default vehicle.

### 3. Explore Screen & Real-time Location
- **Action**: Tap the **Explore** tab in the bottom bar.
- **Action**: Grant location permission when prompted. Notice the top bar displaying real-time Haversine distance badges (e.g., `1.2 km away`).
- **Action**: Try the filter chips:
  - **Sort Dropdown**: Sort by *Nearest*, *Cheapest*, or *Top rated*.
  - **Compatible**: Toggle *Compatible (Tata Nexon EV)* to filter chargers matching CCS2 / Type 2.
  - **Available Now**: Filter spots available at the current time.
  - **Saved**: Filter spots saved in your favorites.
  - **Recent Searches**: Tap on suggested search chips like *Bandra*, *Wakad*, or *Powai*.
- **Action**: Toggle the Map/List icon to view OpenStreetMap tiles (`OsmdroidMapView`) with custom markers.
- **Action**: Tap a marker on the map to display the bottom overlay preview card with **"Navigate"** (launches Google Maps) and **"Book Spot"** buttons.

### 4. 🚨 SOS Low Battery Emergency Mode
- **Action**: On the Explore screen, tap the prominent red **"SOS"** button in the top bar.
- **Action**: Adjust the current EV battery slider to `8%`.
- **Action**: View the top 3 nearest compatible fast chargers calculated in real-time:
  - Distance in km
  - Estimated minutes to reach assuming 25 km/h city traffic
  - Estimated cost to charge to 80% (`neededKwh / powerKw * pricePerHour`)
- **Action**: Tap **"Book Now"** on the top recommendation to navigate straight to the listing detail.

### 5. Spot Detail, Vehicle Compatibility & Booking
- **Action**: View the spot details. Observe the prominent green compatibility badge: `✔ Works with your Tata Nexon EV (CCS2)`.
- **Action**: Select date (Today / Tomorrow), start time, and duration (e.g., 2 hours).
- **Action**: Tap **"Proceed to Payment"**.
- **Action**: Review the price breakdown: Base Rate + PlugNPark Platform Fee (10%) + GST (18%).
- **Action**: Select payment method (UPI / Card / Wallet) and tap **"Pay & Confirm Reservation"**.
- **Action**: On the Confirmation screen, view the generated **ZXing QR Code** for check-in and tap **"Navigate to Spot"**.
- **Action**: Notice that a WorkManager 15-minute start reminder is scheduled automatically and a confirmation notification is stored in Room.

### 6. Host Dashboard & Instant Requests
- **Action**: Switch to the **Host** tab in the bottom bar.
- **Action**: View the **Revenue Breakdown**: Gross Sales, PlugNPark Platform Fee (10%), and Net Host Revenue.
- **Action**: View the **Real Weekly Revenue Bar Chart** generated from completed bookings.
- **Action**: See incoming booking requests for non-instant listings with **Accept** and **Decline** buttons. Accepting notifies the seeker instantly!
- **Action**: Under **My Managed Spots**, try the active/paused toggle switch and test the **Delete Listing** confirmation dialog.
- **Action**: Tap **"Add Spot"** to open the weekly availability schedule editor (day toggles + time pickers) and multi-photo picker.

### 7. Notifications Center
- **Action**: Return to the Explore screen and notice the red badge on the **Bell Icon** in the top bar.
- **Action**: Tap the Bell Icon to open the **Notifications Screen**.
- **Action**: View booking confirmations, scheduled start reminders, and refund notifications with options to **Mark as Read** or **Mark All Read**.

---

## ⚡ Running the Project

1. Open the project in **Android Studio Ladybug (2024.2.1+)** or newer.
2. Ensure JDK 17 or JDK 21 is selected in Gradle Settings.
3. Sync Gradle and run `app` on an Android Emulator or physical device running Android 8.0+ (API 26+).
4. Run tests or build using `./gradlew app:assembleDebug`.

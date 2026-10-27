# ⚡ PlugNPark - Peer-to-Peer EV Charging & Parking Marketplace India

**PlugNPark** is a native Android application (Kotlin, Jetpack Compose, Material 3) that creates a peer-to-peer marketplace for renting out idle home EV chargers and unused private parking spots across Indian cities (Mumbai, Pune, etc.).

---

## 🚀 Concept & Problem Solved

As EV adoption accelerates rapidly across India (Tata Nexon EV, MG ZS EV, Ather, Ola), urban drivers face two major bottlenecks:
1. **EV Range & Charger Scarcity:** Long queues and sparse public fast-charging infrastructure.
2. **Urban Parking Congestion:** High commercial parking rates and difficulty finding safe, covered parking in busy hubs like BKC, Bandra, Andheri, Wakad, Hinjewadi, and Kothrud.

**PlugNPark** solves this by unlocking thousands of private home wallboxes, gated driveways, and stilt parking spaces. Homeowners earn daily passive income, while EV drivers gain instant access to affordable, nearby charging and parking.

---

## ✨ Key Features

- **Splash & 3-Slide Onboarding:** Animated splash screen and onboarding walkthrough (*"Charge at a neighbour's home"*, *"Park in unused spots"*, *"Earn from idle space"*).
- **Mock Authentication & Role Choice:** Phone number (+91) login with 6-digit OTP (**Demo Mode**) and role selection (*Seeker*, *Host*, or *Both*) saved via **Jetpack DataStore Preferences**.
- **Interactive Explore Screen:**
  - Segmented toggle (`All` / `⚡ EV Chargers` / `🅿️ Parking Spots`).
  - View Switch: Interactive **osmdroid OpenStreetMap** vs. **List View**.
  - **Haversine Distance Sorting:** Calculates exact distance in kilometers from current location (defaults to Mumbai City Centre if location permission is denied) and sorts listings automatically.
  - **Coil Photo Loading & Badges:** Verified badges, rating pills, price per hour.
  - **Filter Bottom Sheet:** Sliders for Max Price (₹30–₹300/hr) and Distance (1–50 km), Connector filter (Type 2, CCS2, 15A), Covered/CCTV filters, and Verified-only toggle.
- **Detailed Listing View:** Host contact info, EV charger safety checklist (Auto-cutoff MCB breaker, 5kg fire extinguisher, surge protection), parking specs, availability window, and reviews.
- **Smart Booking & Overlap Check:**
  - Date picker, start time picker, and duration selector (1 to 12 hours).
  - **Overlap Validation:** Queries Room database for existing bookings on the spot and blocks overlapping time slots.
  - **Price Breakdown:** Base amount + 10% Platform Fee + 18% GST on Fee.
- **Mock Checkout & Payment:** UPI (GPay/PhonePe/Paytm), Credit/Debit Card, and Wallet options with a 2-second fake bank processing simulation.
- **Booking Confirmation & QR Code:**
  - Unique Booking ID with a **ZXing-generated QR Code**.
  - **Google Maps Navigation Intent:** Opens Google Maps directly with spot coordinates.
- **Local Notifications:** Immediate booking confirmation notification and 15-minute start reminder notification.
- **My Bookings Screen:**
  - Tabs for **Upcoming**, **Past**, and **Cancelled**.
  - **Cancellation Policy:** Full refund allowed if cancelled > 1 hour before start time.
  - **Rating & Review Dialog:** Post-stay rating and review submission that updates listing average ratings in Room.
- **Host Dashboard:**
  - Earnings cards (**Today**, **This Week**, **Total Revenue**).
  - Custom **Compose Canvas Bar Chart** displaying weekly earnings (Mon–Sun).
  - Active/Paused listing toggle switch.
  - **3-Second Electrician Certificate Verification:** Mock certificate upload that transforms unverified spots into green **"✔ Certified & Verified"** spots.
- **Add/Edit Listing Wizard:** Gallery photo picker (`ActivityResultContracts.GetContent()`), osmdroid tap-to-pin location picker, connector/power specs, and pricing.
- **Profile & Settings:** Edit display name, switch active role, edit registered EV model & specs, expandable FAQ accordion, About PlugNPark, and session Logout.

---

## 🛠 Architecture & Tech Stack

- **Language:** Kotlin 2.1
- **UI Framework:** Jetpack Compose with Material 3 (Green `#00E676` & Electric-Blue `#00B0FF` theme)
- **Architecture:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow
- **Local Database:** Room ORM with KSP annotation processing
- **Session Persistence:** Jetpack DataStore Preferences
- **Mapping & Geolocation:** osmdroid OpenStreetMap + Haversine Formula
- **QR Code Generation:** ZXing Core
- **Image Loading:** Coil Compose
- **Async Execution:** Kotlin Coroutines & `StateFlow`

---

## 📦 Setup & Run Instructions

### Prerequisites
- **Android Studio:** Studio Ladybug / 2024.2+
- **JDK:** Java 11 or higher
- **Android SDK:** SDK 35 / Android 15 (minSdk 35)

### Steps
1. Open Android Studio and select **Open Project** -> `PlugNPark`.
2. Sync Project with Gradle Files (`Gradle Sync`).
3. Select an Android Emulator or connected physical device.
4. Run the app (`Shift + F10` or click **Run 'app'**).

To build via command line:
```bash
./gradlew app:assembleDebug
```

---

## 🎬 Short Demo Walkthrough Script

1. **Launch App:**
   - App opens to Splash Screen and transitions to 3-Slide Onboarding.
   - Click **"Next"** through the slides and tap **"Get Started"**.
2. **Mock Login & Role Selection:**
   - Enter `9876543210` and tap **"Send Verification Code"**.
   - Enter `123456` in the DEMO MODE OTP screen and tap **"Verify & Proceed"**.
   - Select **"Both (Recommended)"** and tap **"Complete Registration"**.
3. **Explore Spots:**
   - Search *"Bandra"* or filter by `⚡ EV Chargers`.
   - Tap the **Filter Icon** to adjust max price or distance.
   - Toggle to **Map View** to see osmdroid pins across Mumbai; tap a marker to view the bottom preview card.
4. **Book a Charger:**
   - Tap **"Fast 22kW AC Charger at Hill Road Villa"**.
   - Review host details, safety checklist, and reviews.
   - Tap **"Book Spot Now"**.
   - Choose Date (*Today*), Start Time (*10:00 AM*), Duration (*2 hours*). Notice the clear price breakdown (Base + Fee + GST).
   - Tap **"Proceed to Payment"**, pick **UPI**, and tap **"Pay Now"**.
   - Observe the 2-second payment processing animation.
5. **Confirmation & QR Code:**
   - View the generated **Booking ID** and **ZXing QR Code**.
   - Tap **"Navigate in Google Maps"** to test location intent.
   - Receive local push notification for booking confirmation.
6. **Manage Bookings & Host Dashboard:**
   - Switch to **Bookings** tab to see your active reservation.
   - Switch to **Host** tab to inspect earnings cards, Canvas bar chart, upcoming bookings, and tap **"Upload Electrician Certificate"** to watch the 3-second verification animation turn green.
7. **Profile Settings:**
   - Go to **Profile** tab, tap edit on Name/Vehicle, expand the FAQ accordion, and test **Log Out**.

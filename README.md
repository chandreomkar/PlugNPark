# PlugNPark ⚡🅿️ - Peer-to-Peer EV Charging & Parking Marketplace

**PlugNPark** is a native Android application built entirely with modern Android development practices (Kotlin, Jetpack Compose). It solves urban EV charging anxiety and parking shortages in Indian megacities by connecting EV owners with private hosts who have idle driveway chargers and parking spots.

---

## 🛠 Tech Stack & Architecture

- **IDE**: Android Studio Ladybug (2024.2.1+)
- **SDK**: `minSdk 26`, `targetSdk 37`
- **Language**: Kotlin 2.1
- **UI Toolkit**: Jetpack Compose (Material 3, Dark/Light theme support)
- **Local Database**: Room DB v2.8 (KSP, `exportSchema = true`) with relational entities
- **Mapping & GIS**: osmdroid (OpenStreetMap) with a live **CARTO Voyager fallback** and custom drawn markers
- **Location Services**: Google Play Services Location (`FusedLocationProviderClient`)
- **Background Tasks**: Android Jetpack WorkManager (for scheduled 15-minute start reminders)
- **Session Management**: Android Jetpack DataStore Preferences
- **Architecture**: MVI/MVVM with Unidirectional Data Flow, Repository Pattern, and `StateFlow`
- **Utilities**: Coil Compose (Images), ZXing Core (QR Codes)

---

## 🗄 Database Schema (Room v2)

The application relies on a local relational database with 11 core entities, strictly enforced foreign keys (`CASCADE` / `SET_NULL`), and compound indices for performance. Money is stored in integer **paise** to avoid floating-point loss, and times are in **Epoch Millis**.

```text
 +------------------+           +------------------+           +------------------+
 |       User       |           |     Vehicle      |           |     Listing      |
 +------------------+           +------------------+           +------------------+
 | id (PK)          |<---------1| id (PK)          |     +---->| id (PK)          |
 | name, phone      |           | ownerId (FK)     |     |     | hostId (FK)      |
 | email, role      |           | make, model      |     |     | type, title      |
 | walletBalance    |           | vehicleType, isEv|     |     | city, lat, lng   |
 | createdAt        |           | connectorType    |     |     | pricePerHourPaise|
 +------------------+           | batteryKwh       |     |     | isVerified       |
          ^                     | isDefault        |     |     | isActive         |
          |                     +------------------+     |     | instantBook      |
          |                                              |     | avgRating, count |
          |                     +------------------+     |     +------------------+
          +--------------------1|     Booking      |     |       ^   ^   ^   ^
          |                     +------------------+     |       |   |   |   |
          |                     | id (PK)          |     |       |   |   |   |
          |                     | listingId (FK)---+-----+       |   |   |   |
          +--------------------+| seekerId (FK)    |             |   |   |   |
          |                     | vehicleId (FK)   |             |   |   |   |
          |                     | startTime, end.. |             |   |   |   |
          |                     | totalPaise, fee..|             |   |   |   |
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
          |                     | AvailabilityRule |                         |
          |                     +------------------+                         |
          |                     | id (PK)          |                         |
          |                     | listingId (FK)---+-------------------------+
          |                     | dayOfWeek        |
          |                     | start/endMinute  |
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
*(Note: ListingPhotos & Favorites omitted for brevity)*

---

## 🚀 Presentation Demo Script

Follow these steps to demonstrate the app's full capabilities during a presentation:

1. **Onboarding & Login (DataStore & Room)**
   - Launch the app. Swipe through the onboarding carousel.
   - Enter any 10-digit phone number (e.g., `9876543210`) and a mock 6-digit OTP. 
   - Select **"Both (Seeker & Host)"**. The app creates a local user profile via Room.

2. **Explore Map & Fallback Architecture (osmdroid & CARTO)**
   - On the Explore screen, toggle to the **Map View**.
   - Explain that tiles are loaded from OpenStreetMap (MAPNIK). 
   - *Technical highlight*: If tiles fail to load within 8 seconds, the app seamlessly falls back to CARTO Voyager tiles with proper attribution.

3. **My Location (FusedLocationProviderClient)**
   - Tap the **My Location FAB**. 
   - The map animates smoothly to your device's actual GPS coordinates (using Google Play Services Fused Location API) and places a distinct blue dot with an accuracy circle.

4. **🚨 SOS Low Battery Flow (Emergency Routing)**
   - Tap the prominent red **SOS** button on the top bar.
   - Adjust the battery slider (e.g., 8%). 
   - The app dynamically calculates the top 3 nearest *compatible* fast chargers. It calculates ETA assuming a 25 km/h city speed, and estimates the cost to charge the vehicle to 80% capacity.
   - Tap **"Book Now"** on a recommendation.

5. **Booking & Vehicle Compatibility**
   - On the Listing Detail page, highlight the **Compatibility Badge** (e.g., green `✔ Works with your Tata Nexon EV (CCS2)`).
   - Select a duration (e.g., 2 hours) and tap **Proceed to Payment**. 
   - View the exact price breakdown (Base rate + 10% Platform fee + 18% GST).

6. **Mock Payment & QR Code Confirmation**
   - Tap **Pay & Confirm Reservation**. 
   - On the Confirmation screen, display the dynamically generated **ZXing QR Code** used for host check-in.
   - Mention the **"Navigate"** button launches Google Maps intents (`geo:` / `https:`).
   - *Technical highlight*: A `OneTimeWorkRequest` is dispatched to Android's **WorkManager** to trigger a local push notification 15 minutes prior to the booking start time.

7. **Host Dashboard**
   - Switch to the **Host** tab in the bottom navigation bar.
   - Show the **Real Weekly Revenue Bar Chart** (calculated live from `COMPLETED` bookings).
   - Scroll to **Pending Booking Requests**. Accept a request—this triggers a database transaction to update the status and send an `AppNotification` to the seeker.
   - Show the **Add Spot** FAB, featuring a custom weekly schedule editor.

8. **Notifications Screen**
   - Go back to the Explore screen and tap the **Bell Icon** (with the red unread badge).
   - View the history of system notifications (Booking confirmed, Host approvals, Charging completed).
   - Tap **"Mark all read"**.

---

## 🚧 Known Limitations

As a student/prototype project, the following elements are simplified:
- **Local-Only Database**: Data is stored locally in SQLite (Room) and does not currently sync to a cloud backend. Pre-seeded mock listings are generated on first launch.
- **Mock Authentication**: OTP is simulated. Any 6-digit input is accepted.
- **Mock Payments**: The app calculates accurate pricing/GST and simulates a loading spinner, but no real financial transactions (Razorpay/Stripe) take place.
- **Image Hosting**: Uploading photos in the "Add Spot" flow accepts raw URLs instead of multipart image uploads to AWS S3/Firebase Storage.

---

## 🔮 Future Scope

- **Cloud Migration**: Refactor the Repository layer to sync Room databases with Firebase Firestore or Supabase for real-time multi-device sync.
- **Hardware / IoT Integration**: Integrate with smart EV wallboxes (via OCPP protocols) so the app can actually start/stop electricity flow and monitor live kWh consumption.
- **Sensor-based Occupancy**: Integrate with ultrasonic or camera-based parking sensors to automatically mark a spot as "Occupied" or "Available".
- **Real Payment Gateway**: Integrate Razorpay SDK for UPI, Card, and Wallet payments with automated vendor payouts to Hosts.

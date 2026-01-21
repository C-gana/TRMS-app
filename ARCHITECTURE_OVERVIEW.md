# TRMS Driver App - Complete Architecture Overview

## 📱 Application Structure (Modules 1 + 2)

```
TRMS Driver Mobile App
│
├── 🔐 MODULE 1: Authentication & Duty Management
│   ├── LoginActivity
│   │   ├── Login form (phone + password)
│   │   ├── JWT token authentication
│   │   └── Remember me functionality
│   │
│   ├── DutyStatusActivity
│   │   ├── Start/End Duty toggle
│   │   ├── Location permission handling
│   │   ├── Duty duration tracking
│   │   └── Today's summary display
│   │
│   └── Data Layer
│       ├── AuthRepository
│       ├── AuthApiService
│       ├── TokenManager (encrypted storage)
│       └── Models: Driver, LoginRequest, DutyStatusRequest
│
└── 📊 MODULE 2: Real-Time Dashboard
    ├── MainActivity (Dashboard)
    │   ├── 4-Seat Grid (2×2 layout)
    │   ├── Vehicle header card
    │   ├── Today's summary card
    │   ├── Auto-refresh (5s interval)
    │   └── Boarding recording
    │
    ├── UI Components
    │   ├── SeatCardBinder (seat data binding)
    │   ├── 4 Seat states (Vacant, Awaiting, Active, Approaching)
    │   └── Material Design 3 cards
    │
    └── Data Layer
        ├── DashboardViewModel
        ├── DashboardRepository
        ├── DashboardApiService
        └── Models: DashboardResponse, SeatStatus, BoardingRequest
```

---

## 🔄 App Flow Diagram

```
[App Launch]
     ↓
┌────────────────┐
│ LoginActivity  │ ← Entry point
│ (Module 1)     │
└────────┬───────┘
         │ Login successful
         ↓
┌────────────────────┐
│ DutyStatusActivity │
│ (Module 1)         │
│                    │
│ ┌────────────────┐ │
│ │ OFF DUTY       │ │ ← Initial state
│ │ [Start Duty]   │ │
│ └────────────────┘ │
└─────────┬──────────┘
          │ Start duty
          ↓
┌─────────────────────────────────┐
│ MainActivity (Dashboard)        │
│ (Module 2)                      │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Vehicle Header              │ │
│ │ TRM-BT-001 • [ON DUTY]      │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌───────────┬───────────┐      │
│ │ SEAT 1    │ SEAT 2    │      │
│ │ [Vacant]  │ [Awaiting]│      │
│ ├───────────┼───────────┤      │
│ │ SEAT 3    │ SEAT 4    │      │
│ │ [Active]  │[Approach] │      │
│ └───────────┴───────────┘      │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Today's Summary             │ │
│ │ 👥 15  💰 22,500 MK         │ │
│ └─────────────────────────────┘ │
│                                 │
│ [Auto-refresh: 5s]              │
└─────────┬───────────────────────┘
          │
          ├─→ Tap vacant seat → Record boarding
          ├─→ End duty → Back to DutyStatusActivity
          └─→ Logout → Back to LoginActivity
```

---

## 🏗️ MVVM Architecture Pattern

```
┌─────────────────────────────────────────────────────────┐
│                        VIEW LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │LoginActivity │  │DutyActivity  │  │MainActivity  │  │
│  │  (Module 1)  │  │  (Module 1)  │  │  (Module 2)  │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
└─────────┼──────────────────┼──────────────────┼─────────┘
          │                  │                  │
          │ observes         │ observes         │ observes
          │ LiveData         │ LiveData         │ LiveData
          ↓                  ↓                  ↓
┌─────────────────────────────────────────────────────────┐
│                     VIEWMODEL LAYER                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │LoginViewModel│  │DutyViewModel │  │Dashboard     │  │
│  │              │  │              │  │ViewModel     │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
└─────────┼──────────────────┼──────────────────┼─────────┘
          │                  │                  │
          │ calls            │ calls            │ calls
          ↓                  ↓                  ↓
┌─────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                      │
│  ┌──────────────┐                    ┌──────────────┐   │
│  │AuthRepository│                    │Dashboard     │   │
│  │              │                    │Repository    │   │
│  └──────┬───────┘                    └──────┬───────┘   │
└─────────┼──────────────────────────────────┼───────────┘
          │                                   │
          │ uses                              │ uses
          ↓                                   ↓
┌─────────────────────────────────────────────────────────┐
│                      DATA LAYER                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │AuthApiService│  │TokenManager  │  │Dashboard     │  │
│  │  (Retrofit)  │  │  (Encrypted) │  │ApiService    │  │
│  └──────┬───────┘  └──────────────┘  └──────┬───────┘  │
└─────────┼──────────────────────────────────┼───────────┘
          │                                   │
          └───────────────┬───────────────────┘
                          │ HTTP
                          ↓
                   ┌──────────────┐
                   │ Backend API  │
                   │  (Node.js)   │
                   └──────────────┘
```

---

## 📦 Package Structure

```
com.cgana.trmsdriver/
│
├── MainActivity.java (Module 2 Dashboard)
│
├── ui/
│   ├── auth/
│   │   ├── LoginActivity.java (Module 1)
│   │   ├── LoginViewModel.java
│   │   └── LoginViewModelFactory.java
│   │
│   ├── duty/
│   │   ├── DutyStatusActivity.java (Module 1)
│   │   ├── DutyStatusViewModel.java
│   │   └── DutyStatusViewModelFactory.java
│   │
│   └── dashboard/ (Module 2)
│       ├── DashboardViewModel.java
│       ├── DashboardViewModelFactory.java
│       └── SeatCardBinder.java
│
├── data/
│   ├── api/
│   │   ├── ApiConfig.java
│   │   ├── RetrofitClient.java
│   │   ├── AuthApiService.java (Module 1)
│   │   └── DashboardApiService.java (Module 2)
│   │
│   ├── repository/
│   │   ├── AuthRepository.java (Module 1)
│   │   └── DashboardRepository.java (Module 2)
│   │
│   ├── local/
│   │   └── TokenManager.java (Module 1, used by both)
│   │
│   └── model/
│       ├── Driver.java (Module 1)
│       ├── LoginRequest.java (Module 1)
│       ├── LoginResponse.java (Module 1)
│       ├── DutyStatusRequest.java (Module 1)
│       ├── DutyStatusResponse.java (Module 1)
│       ├── DashboardResponse.java (Module 2)
│       ├── SeatStatus.java (Module 2)
│       ├── BoardingRequest.java (Module 2)
│       └── BoardingResponse.java (Module 2)
│
├── fcm/
│   └── FCMService.java (Firebase Cloud Messaging)
│
└── utils/
    └── (Utility classes)
```

---

## 🎨 Resource Structure

```
app/src/main/res/
│
├── layout/
│   ├── activity_login.xml (Module 1)
│   ├── activity_duty_status.xml (Module 1)
│   ├── activity_main.xml (Module 2 - Dashboard)
│   ├── item_seat_card.xml (Module 2 - Seat component)
│   └── loading_overlay.xml (Module 2)
│
├── drawable/
│   ├── ic_driver_logo.xml (Module 1)
│   ├── ic_phone.xml (Module 1)
│   ├── ic_lock.xml (Module 1)
│   ├── ic_duty_on.xml (Module 1)
│   ├── ic_duty_off.xml (Module 1)
│   ├── ic_vehicle.xml (Module 2)
│   ├── ic_destination.xml (Module 2)
│   ├── ic_time.xml (Module 2)
│   ├── ic_refresh.xml (Module 2)
│   ├── ic_seats.xml (Module 2)
│   ├── ic_passengers.xml (Module 2)
│   ├── ic_money.xml (Module 2)
│   ├── ic_summary.xml (Module 2)
│   ├── circle_shape.xml (Module 1)
│   ├── duty_badge_background.xml (Module 2)
│   └── status_badge_background.xml (Module 1)
│
├── menu/
│   ├── menu_duty_status.xml (Module 1)
│   └── menu_dashboard.xml (Module 2)
│
├── values/
│   ├── colors.xml (Shared: Module 1 + 2)
│   ├── strings.xml (Shared: Module 1 + 2)
│   ├── dimens.xml (Shared: Module 1 + 2)
│   └── themes.xml (Material Design 3)
│
└── navigation/
    └── nav_graph.xml (Future: for Navigation Component)
```

---

## 🔐 Security & Data Flow

```
┌─────────────────────────────────────────────────────────┐
│                  AUTHENTICATION FLOW                     │
└─────────────────────────────────────────────────────────┘

User enters credentials
         ↓
LoginActivity → LoginViewModel → AuthRepository
         ↓
AuthApiService (Retrofit) → POST /api/mobile/auth/login
         ↓
Backend validates credentials
         ↓
Returns: { token: "jwt...", driver: {...} }
         ↓
TokenManager saves to EncryptedSharedPreferences
         ↓
Navigate to DutyStatusActivity


┌─────────────────────────────────────────────────────────┐
│                  DASHBOARD DATA FLOW                     │
└─────────────────────────────────────────────────────────┘

MainActivity loads
         ↓
DashboardViewModel.loadDashboardData(vehicleId)
         ↓
DashboardRepository.getDashboardStatus(vehicleId)
         ↓
Add JWT token: "Bearer {token}"
         ↓
DashboardApiService → GET /api/mobile/driver/dashboard/{id}
         ↓
Backend returns: { vehicle_id, seats[], todays_stats }
         ↓
LiveData<DashboardResponse> updates
         ↓
MainActivity observes & updates UI
         ↓
Auto-refresh after 5 seconds (loop)


┌─────────────────────────────────────────────────────────┐
│                  BOARDING RECORDING FLOW                 │
└─────────────────────────────────────────────────────────┘

User taps vacant seat
         ↓
Confirmation dialog
         ↓
Request location permission (if needed)
         ↓
Get GPS coordinates (latitude, longitude)
         ↓
DashboardViewModel.recordBoarding(...)
         ↓
DashboardRepository creates BoardingRequest
         ↓
Add JWT token: "Bearer {token}"
         ↓
DashboardApiService → POST /api/mobile/driver/boarding
         ↓
Backend creates journey record
         ↓
Returns: { success: true, journey_id, ... }
         ↓
Show success toast
         ↓
Auto-refresh dashboard (updates seat to "awaiting")
```

---

## 📊 State Management

### Seat States (Module 2):
```
┌──────────┐  Record    ┌────────────────┐  Select      ┌──────────────┐
│  VACANT  │─────────→  │    AWAITING    │──────────→   │    ACTIVE    │
│  (Grey)  │  Boarding  │  DESTINATION   │  Destination │   JOURNEY    │
└──────────┘            │    (Blue)      │              │   (Green)    │
                        └────────────────┘              └──────┬───────┘
                                                                │
                                                                │ Distance < 1km
                                                                ↓
     ┌──────────────────────────────────────────────────────────────┐
     │                    APPROACHING DESTINATION                    │
     │                         (Orange)                              │
     │                        Alert: ⚠️                              │
     └───────────────────────────┬───────────────────────────────────┘
                                 │
                                 │ Passenger arrives
                                 ↓
                            ┌──────────┐
                            │  VACANT  │
                            │  (Grey)  │
                            └──────────┘
```

### Duty Status (Module 1):
```
┌─────────────┐  Start Duty   ┌─────────────┐
│  OFF DUTY   │───────────→   │   ON DUTY   │
│  (Grey)     │ (with GPS)    │  (Green)    │
└─────────────┘               └──────┬──────┘
      ↑                              │
      │                              │
      └──────────────────────────────┘
              End Duty
```

---

## 🎯 Key Features Summary

### Module 1: Authentication & Duty Management
✅ JWT-based authentication
✅ Encrypted token storage
✅ Remember me functionality
✅ Duty status toggle (ON/OFF)
✅ Location permission handling
✅ Duty duration tracking
✅ Today's summary (passengers & revenue)

### Module 2: Real-Time Dashboard
✅ 4-seat grid (2×2 layout)
✅ 4 seat states (Vacant, Awaiting, Active, Approaching)
✅ Auto-refresh every 5 seconds
✅ Pull-to-refresh
✅ Boarding recording with GPS
✅ Distance & ETA display
✅ Timeout counter for awaiting seats
✅ Alert indicator for approaching seats
✅ Vehicle header with duty badge
✅ Today's summary card
✅ Last updated timestamp

---

## 🔧 Technical Stack

### Frontend (Android):
- **Language:** Java
- **Architecture:** MVVM
- **UI Framework:** Material Design 3
- **Networking:** Retrofit 2 + OkHttp
- **JSON:** Gson
- **Storage:** EncryptedSharedPreferences
- **Location:** Google Play Services Location
- **Lifecycle:** AndroidX Lifecycle (ViewModel, LiveData)

### Backend API:
- **Platform:** Node.js
- **Database:** (Assumed SQL/NoSQL)
- **Authentication:** JWT tokens
- **Real-time:** REST API (polling every 5s)

---

## 📱 Supported Android Versions

- **Minimum SDK:** 24 (Android 7.0 Nougat)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

---

## 🚀 Performance Characteristics

### Auto-Refresh:
- **Interval:** 5 seconds
- **Network calls:** ~720 per hour (when app active)
- **Optimization:** Pauses when app in background

### Memory:
- **View caching:** Seat cards reused (no re-inflation)
- **Lifecycle aware:** No memory leaks

### Battery:
- **Location:** Only requested during boarding
- **Background:** Auto-refresh pauses in background

---

**Status:** ✅ Modules 1 + 2 COMPLETE
**Architecture:** ✅ Production-ready MVVM
**Build:** ✅ Compiles successfully (Java 17 required)


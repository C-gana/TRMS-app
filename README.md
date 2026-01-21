# TRMS Driver Mobile App - Module 1 Login & Auth

## Overview
Professional taxi driver app with MVVM architecture, Material Design 3 UI, and Node.js backend integration.

## Module 1: Authentication & Duty Status
✅ **Login Screen** - Material Design 3 with large touch targets
✅ **MVVM Architecture** - Repository pattern with Retrofit + LiveData  
✅ **Navigation Component** - Fragment-based navigation  
✅ **Token Management** - Secure storage with SharedPreferences  
⏳ **Duty Toggle** - Placeholder (needs implementation)  
⏳ **Location Permissions** - Needs implementation  

## Project Structure
```
app/src/main/
├── java/com/cgana/trmsdriver/
│   ├── MainActivity.java                    # Nav host activity
│   ├── data/
│   │   ├── api/
│   │   │   └── ApiConfig.java              # API endpoints & base URL
│   │   ├── local/
│   │   │   └── TokenManager.java           # Secure token/driver storage
│   │   └── model/
│   │       ├── Driver.java                 # Driver entity
│   │       ├── LoginResponse.java          # Login API response
│   │       └── [other models]
│   └── ui/
│       ├── login/
│       │   ├── LoginFragment.java          # Login UI
│       │   ├── LoginViewModel.java         # Login business logic
│       │   ├── LoginRepository.java        # Login API calls
│       │   └── LoginRequest.java           # Login payload
│       └── home/
│           └── HomeFragment.java           # Dashboard placeholder
└── res/
    ├── drawable/                           # Icons & backgrounds
    ├── layout/
    │   ├── activity_main.xml               # NavHost container
    │   ├── activity_login.xml              # Login screen design
    │   ├── fragment_login.xml
    │   └── fragment_home.xml
    ├── navigation/
    │   └── nav_graph.xml                   # Navigation routes
    └── values/
        ├── colors.xml                      # Driver-optimized palette
        ├── dimens.xml                      # Touch targets & spacing
        └── strings.xml                     # UI text
```

## Build Configuration
- **AGP**: 7.4.2 (Java 11 compatible)
- **Compile/Target SDK**: 33
- **Min SDK**: 26 (Android 8.0)
- **Build Tool**: Gradle 8.11.1
- **Language**: Java 11

## Dependencies
```gradle
// UI
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.9.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

// Architecture
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.6.1'
implementation 'androidx.lifecycle:lifecycle-livedata:2.6.1'
implementation 'androidx.navigation:navigation-fragment:2.5.3'
implementation 'androidx.navigation:navigation-ui:2.5.3'

// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

// Firebase
implementation platform('com.google.firebase:firebase-bom:32.7.0')
implementation 'com.google.firebase:firebase-messaging'
implementation 'com.google.firebase:firebase-analytics'

// Maps & Charts
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
implementation 'org.osmdroid:osmdroid-android:6.1.17'
implementation 'com.google.android.gms:play-services-location:21.0.1'

// Storage
implementation 'androidx.room:room-runtime:2.5.2'
implementation 'androidx.work:work-runtime:2.8.1'
```

## Building the Project

### Prerequisites
- Java 11 JDK
- Android SDK 33
- Node.js backend running on port 3000

### Build Commands
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Or use the build script
./build.sh
```

### Build Output
APK: `app/build/outputs/apk/debug/app-debug.apk`

## Backend Configuration

### API Endpoints
Base URL: `http://10.0.2.2:3000/` (emulator → host)  
Login: `POST /api/drivers/login`

### Login Request
```json
{
  "phone_number": "123456789",
  "password": "password123"
}
```

### Login Response
```json
{
  "success": true,
  "token": "jwt_token_here",
  "driver": {
    "driver_id": "DRV001",
    "full_name": "John Doe",
    "phone_number": "123456789",
    "vehicle_registration": "ABC123",
    "on_duty": false
  }
}
```

### Error Response
```json
{
  "success": false,
  "error": "Invalid credentials"
}
```

## Testing

### Test Login Flow
1. Start Node.js backend on host machine
2. Launch app in emulator
3. Enter phone number (9 digits after +265)
4. Enter password
5. Tap "LOGIN TO DRIVE" button
6. Verify navigation to home screen
7. Close and reopen app - should stay logged in

### Test Error Handling
1. Enter invalid credentials
2. Verify error message shows
3. Turn off backend
4. Try login - verify network error shows
5. Leave field empty - verify validation toast

### UI/UX Checks
- [ ] All touch targets ≥ 48dp
- [ ] Login button is 72dp height
- [ ] Text is readable (18sp for important content)
- [ ] Icons are visible and properly sized
- [ ] Error messages appear in red container
- [ ] Loading indicator shows during API call
- [ ] Navigation is smooth

## Design System

### Colors
```xml
Primary: #2196F3 (Blue)
Accent: #FF9800 (Orange)
Success: #4CAF50 (Green)  
Danger: #F44336 (Red)
On Duty: #4CAF50
Off Duty: #9E9E9E
```

### Touch Targets (Driver-Optimized)
- Minimum: 48dp
- Standard buttons: 56dp
- Primary actions: 72dp
- Critical actions (duty toggle): 88dp

### Spacing
- XS: 4dp
- SM: 8dp
- MD: 16dp (default)
- LG: 24dp
- XL: 32dp
- XXL: 48dp

## Known Issues & Limitations
1. **Encryption**: Using regular SharedPreferences (not encrypted) due to security-crypto 1.0.0 API constraints
2. **Safe Args**: Not enabled - using manual navigation IDs
3. **Java Version**: Limited to Java 11 (not 17) 
4. **SDK Version**: Compile SDK 33 (not latest 35)

## Next Steps

### To Complete Module 1
- [ ] Implement home screen duty toggle UI
- [ ] Add duty status API integration
- [ ] Implement location permissions flow
- [ ] Add notification permission (Android 13+)
- [ ] Add FCM token registration
- [ ] Add logout functionality
- [ ] Improve error handling & retry logic
- [ ] Add input validation feedback
- [ ] Add haptic feedback

### Future Modules
- Module 2: Real-time location tracking
- Module 3: Booking management
- Module 4: Route navigation
- Module 5: Passenger communication
- Module 6: Earnings dashboard
- Module 7: Reports & analytics
- Module 8: Settings & preferences

## Troubleshooting

### Build Fails with "Android Gradle plugin requires Java 17"
- Downgrade AGP to 7.4.2 in `gradle/libs.versions.toml`
- Or install Java 17 and set JAVA_HOME

### ViewBinding classes not generated
```bash
./gradlew clean
./gradlew :app:generateDebugResources
./gradlew assembleDebug
```

### Cannot resolve navigation IDs
- Sync Gradle files
- Rebuild project
- Check `nav_graph.xml` syntax

### Login API call fails
- Check backend is running on port 3000
- Verify emulator can reach host: `adb shell ping 10.0.2.2`
- Check API endpoint path in `ApiConfig.java`
- Enable OkHttp logging to see requests

### ViewBinding import errors
- Ensure `buildFeatures { viewBinding = true }` in build.gradle.kts
- Clean and rebuild project
- Check layout XML files for errors

## Support
For issues or questions about Module 1 implementation, check:
- `MODULE1_IMPLEMENTATION_STATUS.md` - Implementation progress
- Build logs: `build_output.log`
- Gradle reports: `build/reports/`

## License
[Your license here]


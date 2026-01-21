# Build Troubleshooting Guide

## Current Status
The project experienced a build failure after initially showing "BUILD SUCCESSFUL".

## Root Cause Analysis
Based on the IDE errors, there are two issues preventing successful compilation:

### Issue 1: ViewBinding Classes Not Generated
The following binding classes cannot be resolved:
- `FragmentLoginBinding`
- `FragmentHomeBinding`  
- `ActivityMainBinding`
- Navigation IDs (`nav_host_fragment`, `homeFragment`, `action_loginFragment_to_homeFragment`)

**Why**: ViewBinding generation happens during the build process. If the build fails before reaching the ViewBinding generation step, these classes won't exist.

### Issue 2: Type System Confusion (false positive)
```java
// In LoginViewModel.java line 51:
repository.persistLogin(response.body().getToken(), response.body().getUser(), remember);
```

The IDE reports: `'persistLogin(String, Driver, boolean)' cannot be applied to '(String, User, boolean)'`

**Analysis**: This is a false error. The `getUser()` method in `LoginResponse` returns `Driver`:
```java
public Driver getUser() {
    return driver;  // Returns Driver, not User
}
```

## Solution Steps

### Step 1: Clean All Cached Files
```bash
cd /home/c/AndroidStudioProjects/TRMSDriverApp
rm -rf .gradle app/build build .idea
```

### Step 2: Sync Gradle Files
```bash
./gradlew --refresh-dependencies
```

### Step 3: Build with Full Logs
```bash
./gradlew clean assembleDebug --info --stacktrace 2>&1 | tee detailed_build.log
```

### Step 4: Check for Specific Errors
```bash
# Look for compilation errors
grep -A 5 "error:" detailed_build.log

# Look for resource linking errors  
grep -A 5 "resource.*not found" detailed_build.log

# Check final status
grep -E "BUILD (SUCCESSFUL|FAILED)" detailed_build.log
```

## Files to Verify

### Check These Files Exist:
```bash
find app/src/main/res -name "*.xml" | grep -E "fragment_login|fragment_home|activity_main|nav_graph"
```

Expected output:
```
app/src/main/res/layout/activity_main.xml
app/src/main/res/layout/fragment_login.xml
app/src/main/res/layout/fragment_home.xml
app/src/main/res/navigation/nav_graph.xml
```

### Check Java Files:
```bash
find app/src/main/java -name "*.java" | grep -E "LoginFragment|HomeFragment|LoginViewModel|MainActivity"
```

Expected output:
```
app/src/main/java/com/cgana/trmsdriver/MainActivity.java
app/src/main/java/com/cgana/trmsdriver/ui/login/LoginFragment.java
app/src/main/java/com/cgana/trmsdriver/ui/login/LoginViewModel.java
app/src/main/java/com/cgana/trmsdriver/ui/home/HomeFragment.java
```

## Common Issues & Fixes

### Issue: "Cannot resolve symbol 'R'"
**Fix**: Clean and rebuild
```bash
./gradlew clean build
```

### Issue: ViewBinding not generating
**Check**: `build.gradle.kts` has:
```kotlin
buildFeatures {
    viewBinding = true
}
```

**Fix**: Sync Gradle files in Android Studio or run:
```bash
./gradlew --refresh-dependencies
```

### Issue: Navigation IDs not found
**Check**: `nav_graph.xml` exists and is valid XML
```bash
xmllint --noout app/src/main/res/navigation/nav_graph.xml
```

### Issue: Gradle daemon issues
**Fix**: Kill daemon and retry
```bash
./gradlew --stop
./gradlew clean assembleDebug
```

## Android Studio Specific

If using Android Studio:
1. **File → Invalidate Caches / Restart**
2. **Build → Clean Project**
3. **Build → Rebuild Project**
4. **File → Sync Project with Gradle Files**

## Expected Build Time
- **Clean build**: 30-90 seconds
- **Incremental build**: 5-15 seconds

## Success Indicators
```
BUILD SUCCESSFUL in 30s
40+ actionable tasks executed

APK location: app/build/outputs/apk/debug/app-debug.apk
```

## If Build Still Fails

### Check Dependencies
```bash
./gradlew app:dependencies --configuration debugCompileClasspath
```

### Check for Conflicts
```bash
./gradlew app:checkDebugDuplicateClasses
```

### Verify Java Version
```bash
./gradlew -version
# Should show: JVM: 11.x or higher
```

### Check AGP Compatibility
In `gradle/libs.versions.toml`:
```toml
agp = "7.4.2"  # Should work with Java 11
```

## Nuclear Option - Complete Reset
```bash
# Backup your code first!
cd /home/c/AndroidStudioProjects/TRMSDriverApp

# Remove all generated files
rm -rf .gradle .idea app/build build
rm -rf app/.cxx app/.externalNativeBuild

# Clean Gradle cache
rm -rf ~/.gradle/caches

# Rebuild
./gradlew clean assembleDebug --refresh-dependencies
```

## Next Steps After Successful Build

1. Check APK was created:
```bash
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

2. Install to device/emulator:
```bash
./gradlew installDebug
```

3. Test the app:
- Open login screen
- Enter test credentials
- Verify navigation works

## Contact Points
If build continues to fail, check:
1. `build/reports/problems/problems-report.html` - Gradle problems report
2. Android Studio Build window - Detailed error messages
3. `app/build/outputs/logs/` - Build logs

## Summary
The main issue is likely ViewBinding generation being blocked by a compilation error. Once the build succeeds, all binding classes and R IDs will be auto-generated and the errors will disappear.


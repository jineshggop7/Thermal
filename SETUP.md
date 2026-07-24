# Installation & Setup Guide

## Prerequisites

Before building the Thermal Monitor app, ensure you have the following installed:

### 1. Android Studio
- Download from: https://developer.android.com/studio
- Version: Arctic Fox (2020.3.1) or later
- Installation: Follow official installation guide for your OS

### 2. Android SDK
- API 35 (Android 16) - Required for compilation
- API 21+ - For testing on various devices
- Android Build Tools: 35.0.0 or later
- Android Emulator (optional but recommended)

### 3. Java Development Kit (JDK)
- JDK 17 or later
- Set JAVA_HOME environment variable

### 4. Gradle
- Handled by Android Studio's Gradle wrapper
- No separate installation needed

## Installation Steps

### Option 1: Direct APK Installation (Fastest)
1. Download **[Thermal.apk](./Thermal.apk)** from the root directory.
2. Transfer the APK to your Android device.
3. Open the file on your device and install (allow "Install from Unknown Sources" if prompted).

### Option 2: Build from Source
#### Step 1: Clone/Download Project

```bash
cd /path/to/your/workspace
# Project is already in: ThermalMonitor
```

### Step 2: Open in Android Studio

1. Launch Android Studio
2. Click "Open"
3. Navigate to the project folder
4. Select the folder and click OK
5. Wait for Gradle sync to complete

### Step 3: Configure Android SDK

1. Go to: File → Settings → Appearance & Behavior → System Settings → Android SDK
2. Verify these are installed:
   - Android API 35
   - Android Build-Tools 35.0.0
   - Android SDK Platform 35
3. Click "Apply" and "OK"

### Step 4: Build Project

```bash
./gradlew clean build
```

Or use Android Studio:
1. Build → Clean Project
2. Build → Make Project

### Step 5: Run App

#### On Emulator:
1. Device Manager → Create Virtual Device
2. Select Android 16 (API 35) image
3. Click Run or press Shift+F10

#### On Physical Device:
1. Enable Developer Mode (Settings → About Device → Tap Build Number 7 times)
2. Enable USB Debugging (Settings → Developer Options)
3. Connect via USB
4. Click Run or press Shift+F10

## Troubleshooting

### Issue: Gradle Sync Failed
**Solution:**
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Issue: Cannot Find SDK
**Solution:**
- Check local.properties file exists with correct SDK path
- Go to Tools → SDK Manager and reinstall required components

### Issue: Temperature Reading Returns 0
**Solution:**
- This is expected on emulator
- Test on real device with actual thermal zone files
- Fallback temperature is 25°C

### Issue: Permissions Not Granted
**Solution:**
- Runtime permissions are requested on app start
- Grant permissions when prompted
- For Android 13+, POST_NOTIFICATIONS requires runtime permission

### Issue: Floating Window Not Showing
**Solution:**
- Ensure SYSTEM_ALERT_WINDOW permission is granted
- Check Settings → Apps → Permissions → Display over other apps
- Restart the app

## Testing

### Unit Testing
```bash
./gradlew test
```

### Instrumented Testing (on device/emulator)
```bash
./gradlew connectedAndroidTest
```

## APK Generation

### Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

## Device Requirements

- **Minimum Android Version**: 5.0 (API 21)
- **Target Android Version**: 16 (API 35)
- **RAM**: 256 MB minimum
- **Storage**: 50 MB for app
- **Thermal Zone Support**: Required for accurate readings

## Important Notes

1. **Temperature Reading**: Only works on real devices with accessible thermal zone files
2. **Floating Window**: Requires permission grant on first use
3. **Database**: Automatically created in app's private storage
4. **Permissions**: Only SYSTEM_ALERT_WINDOW and POST_NOTIFICATIONS are used

## Development Environment Setup

### IDE Configuration
1. File → Settings → Editor → Code Style → Kotlin
2. Set code style preferences
3. Enable Kotlin code inspections

### Version Control (Git)
```bash
cd TempMonitor
git init
git add .
git commit -m "Initial commit: Thermal Monitor app"
```

## Build Variants

The app is configured with:
- **Debug Variant**: For development and testing
- **Release Variant**: For production with ProGuard minification

## API Reference

### Main Classes
- `MainActivity.kt` - Entry point and tab management
- `TemperatureUtils.kt` - Temperature reading and formatting
- `FloatingWindowService.kt` - Overlay window management
- `AlarmService.kt` - Alarm settings and checking
- `TemperatureDatabase.kt` - Local database management

### Key Permissions
- `SYSTEM_ALERT_WINDOW` - Draw overlay windows
- `POST_NOTIFICATIONS` - Send notifications (Android 13+)

## Production Deployment

1. Update `versionCode` and `versionName` in build.gradle
2. Generate signed APK/AAB
3. Create keystore:
   ```bash
   keytool -genkey -v -keystore thermal_monitor.keystore \
       -alias thermal_app -keyalg RSA -keysize 2048 -validity 10000
   ```
4. Sign and upload to Play Store

## Support & Documentation

- Official Android Docs: https://developer.android.com/docs
- Room Database Guide: https://developer.android.com/training/data-storage/room
- Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
- MPAndroidChart: https://github.com/PhilJay/MPAndroidChart/wiki

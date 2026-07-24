# Thermal Monitor - Android App

A comprehensive thermal monitoring application for Android devices (API 21+, targets API 35).

## Features

### 1. **Homepage**
- Real-time CPU temperature reading
- Real-time battery temperature reading
- Visual progress bars for temperature levels
- Temperature status indicator (Cool, Normal, Warm, Hot, Very Hot)
- Toggle button for floating window

### 2. **Temperature Gradient Theme**
- Automatic color gradient based on temperature
- Blue (cool) → Red (hot) gradient transition
- Updates in real-time across all UI elements
- Floating window also adapts color gradient

## Quick Install

For immediate testing, you can install the pre-built APK directly from the repository:
1. Download **[Thermal.apk](./Thermal.apk)** from the root of this project.
2. Transfer it to your Android device.
3. Open the APK and follow the installation prompts (you may need to enable "Install from Unknown Sources").

### 3. **History Tab**
- View temperature trends over time
- Three time ranges: 1 Hour, 1 Day, 1 Week
- Line chart visualization with trend lines for both CPU and Battery
- Refresh button for manual updates
- Real-time database storage

### 4. **Alarm Tab**
- Set temperature thresholds for CPU, Battery, or Both
- Dropdown menu for alarm type selection
- Temperature input field with validation (0-100°C)
- Enable/Disable alarm controls
- 5 beep notification alerts with vibration
- Notification updates when threshold is reached

### 5. **Floating Window**
- Always-on-top window showing current temperatures
- Draggable interface
- Gradient color theme based on current temperatures
- Updates every 2 seconds
- Start/Stop from Home tab

## Permissions (Minimal Required)
- `SYSTEM_ALERT_WINDOW` - For floating window overlay
- `POST_NOTIFICATIONS` - For alarm notifications (Android 13+)

## Architecture

### Project Structure
```
com.example.thermalmonitor/
├── MainActivity.kt
├── model/
│   └── TemperatureData.kt (Data classes, Room entities)
├── database/
│   ├── TemperatureDao.kt
│   └── TemperatureDatabase.kt (Room database setup)
├── service/
│   ├── FloatingWindowService.kt (Overlay window)
│   ├── AlarmService.kt (Alarm management)
│   ├── AlarmNotificationManager.kt
│   └── AlarmReceiver.kt
├── ui/
│   ├── fragment/
│   │   ├── HomeFragment.kt
│   │   ├── HistoryFragment.kt
│   │   └── AlarmFragment.kt
│   └── adapter/
│       └── TabsPagerAdapter.kt
└── utils/
    └── TemperatureUtils.kt (Temperature reading & formatting)
```

## Technology Stack

- **Language**: Kotlin
- **Target API**: 35 (Android 16)
- **Minimum API**: 21 (Android 5.0)
- **UI Framework**: Android Material Components
- **Architecture**: MVVM with Fragments & ViewPager2
- **Database**: Room (SQLite)
- **Charts**: MPAndroidChart v3.1.0
- **Coroutines**: Kotlin Coroutines for async operations
- **Notifications**: Android NotificationCompat

## Build & Run

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17+
- Android SDK 35 (API Level 35)
- Android NDK (optional)

### Setup
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Create an emulator or connect a device (Android 5.0+)
5. Run the app

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Clean build
./gradlew clean build
```

## Temperature Reading

The app reads temperature from multiple Android system thermal zones:
- `/sys/class/thermal/thermal_zone*/temp`
- `/sys/class/hwmon/hwmon0/temp*_input`

Battery temperature is read using BatteryManager API.

## Database

Room Database stores temperature readings with timestamps. Auto-cleanup can be implemented to delete old data (older than 30 days) to manage storage.

## Alarm System

1. User sets threshold and alarm type
2. Main activity continuously monitors temperatures
3. When threshold is reached, alarm triggers
4. Notification with sound (5 beeps) is shown
5. Vibration pattern: 500ms on, 250ms off, repeated
6. User can disable alarm from Alarm tab

## Color Gradient

- **0°C**: Blue RGB(50, 50, 255)
- **40°C**: Purple-ish RGB(152, 50, 128)
- **80°C**: Red RGB(255, 50, 0)

Linear interpolation between these values for smooth gradient.

## Permissions Justification

1. **SYSTEM_ALERT_WINDOW**: Required to display floating window overlay
2. **POST_NOTIFICATIONS**: Required for alarm notifications on Android 13+

No other permissions are requested, ensuring privacy and security.

## Future Enhancements

- Export temperature data as CSV
- Customizable alarm sounds
- Multiple alarm thresholds
- Background monitoring service
- Widget for home screen
- Temperature statistics and analytics
- GPU temperature monitoring (if available)

## License

This project is open-source and available for educational and personal use.

## Support

For issues or feature requests, please refer to the development documentation.

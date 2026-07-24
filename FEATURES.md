# Thermal Monitor - Complete Feature Documentation

## 📱 Application Overview

**Thermal Monitor** is an Android application designed to monitor device thermal conditions in real-time. It reads CPU and battery temperatures, displays trends, and alerts users when temperatures exceed set thresholds.

### Target Specifications
- **Platform**: Android
- **Target API**: 35 (Android 16)
- **Minimum API**: 21 (Android 5.0)
- **Language**: Kotlin
- **Architecture**: MVVM with Fragments

---

## ✨ Features

### 1. 🏠 Homepage
**Location**: Tab 1

#### Real-Time Temperature Display
- **CPU Temperature**: Reads from system thermal zone files
  - Path: `/sys/class/thermal/thermal_zone*/temp`
  - Updates every 1 second
  - Displayed in Celsius with 1 decimal place
  
- **Battery Temperature**: Reads using BatteryManager API
  - Provides accurate battery thermal state
  - Updates every 1 second
  - Displayed in Celsius

#### Visual Indicators
- Progress bars for both CPU and Battery (0-80°C scale)
- Temperature status text (Cool/Normal/Warm/Hot/Very Hot)
- Large, readable temperature displays

#### Floating Window Control
- Toggle button to enable/disable overlay window
- Always-on-top floating display
- Draggable interface

#### Dynamic Gradient Theme
- Background color automatically transitions from blue (cool) to red (hot)
- Based on maximum temperature (CPU or Battery)
- Smooth gradient for visual appeal

---

### 2. 📊 History Tab
**Location**: Tab 2

#### Time Range Selection
- **1 Hour**: Last 60 minutes of data
- **1 Day**: Last 24 hours of data
- **1 Week**: Last 7 days of data
- Dropdown selector for easy switching

#### Trend Visualization
- **Line Chart** using MPAndroidChart library
- Two separate trend lines:
  - CPU Temperature (Blue line)
  - Battery Temperature (Red line)
- Interactive chart with:
  - Pan/Zoom capabilities
  - Pinch zoom support
  - Touch-enabled navigation

#### Data Management
- Automatic database storage every 5 seconds
- Temperature samples recorded with timestamps
- Refresh button for manual data reload
- Room database for local persistence

#### Chart Features
- Smooth curve rendering
- Circle markers at data points
- Grid background
- Customizable colors
- Legend display

---

### 3. 🚨 Alarm Tab
**Location**: Tab 3

#### Alarm Type Selection
- **Dropdown menu** with three options:
  1. **CPU**: Monitor CPU temperature only
  2. **Battery**: Monitor battery temperature only
  3. **Both**: Monitor both temperatures

#### Threshold Configuration
- Input field for temperature threshold (0-100°C)
- Validation ensures valid temperature range
- Stores settings persistently using SharedPreferences
- Individual thresholds for each alarm type

#### Alarm Controls
- **Set Alarm Button**: Confirms and saves alarm settings
- **Disable Alarm Button**: Turns off current alarm type
- Toast notifications for user feedback

#### Alarm Triggering
- Continuous background monitoring
- Triggers when temperature exceeds threshold
- Multiple alarms can be active simultaneously

#### Notification System
- **Alert Type**: High-priority notification
- **Sound**: 5 sequential beeps (alarm tone)
- **Vibration**: Pattern - 500ms on, 250ms off, repeating
- **Visual**: Notification card with temperature data
- **Action**: Tappable to navigate to app
- **Auto-dismiss**: After user interaction

#### Notification Channel
- Separate notification channel for temperature alerts
- Android 8+ (API 26) support
- Audio attributes set for alarm urgency

---

### 4. 🎨 Temperature Gradient Theme

#### Color Scheme
The app features an **automatic gradient** based on device temperature:

```
Temperature → Color (RGB)
    0°C  → Blue (50, 50, 255)
   20°C  → Light Blue (100, 50, 255)
   40°C  → Purple (152, 50, 128)
   60°C  → Orange-Red (205, 50, 50)
   80°C  → Red (255, 50, 0)
```

#### Applications
- **Homepage**: Background gradient
- **Floating Window**: Updates with temperature
- **History Tab**: Chart markers
- **Progress Bars**: Color-coded thermometers

#### Smooth Transition
- Linear interpolation between colors
- Real-time updates every 1-2 seconds
- Gradual color changes for visual comfort

---

### 5. 🪟 Floating Window (Overlay)

#### Overview
- Always-visible temperature display overlay
- Remains on top of other applications
- Draggable to any screen position
- Temperature updates every 2 seconds

#### Display Content
```
┌─────────────────────────┐
│  CPU: XX.X°C            │
│  Battery: XX.X°C        │
│                         │
│  [Gradient Background]  │
└─────────────────────────┘
```

#### Features
- Gradient background (same as homepage)
- Touchable for repositioning
- Semi-transparent overlay
- Minimal system impact
- Toggle on/off from Home tab

#### Technical Details
- Service: `FloatingWindowService`
- Window Type: TYPE_APPLICATION_OVERLAY (Android 8+)
- Size: 400dp × 200dp
- Gravity: Top-left by default
- Updates via Coroutines every 2 seconds

---

## 🔐 Permissions

### Minimal Permission Model
The app requests **only** permissions that are strictly necessary:

#### 1. SYSTEM_ALERT_WINDOW
```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```
- **Purpose**: Draw floating window overlay
- **Type**: Special permission
- **User Control**: Settings → Apps → Display over other apps
- **Runtime**: Request on first use

#### 2. POST_NOTIFICATIONS (Android 13+)
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```
- **Purpose**: Send alarm notifications
- **Type**: Runtime permission (Android 13+)
- **User Control**: Automatically requested on app launch
- **Impact**: Required for alarm alerts

### Permissions NOT Requested
- ❌ Camera
- ❌ Microphone
- ❌ Location
- ❌ Contacts
- ❌ Photos/Media
- ❌ Calendar
- ❌ SMS
- ❌ Phone

---

## 🗄️ Database Architecture

### Room Database
- **Name**: `temperature_database`
- **Version**: 1
- **Location**: App private storage

### Table: `temperature_data`
```kotlin
@Entity(tableName = "temperature_data")
data class TemperatureData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val cpuTemperature: Float,
    val batteryTemperature: Float,
    val timestamp: Long
)
```

### Queries
- Insert temperature reading
- Fetch all temperatures
- Get temperatures after timestamp
- Get temperatures in date range
- Delete old data (>30 days)

### Storage Management
- Auto-stores every 5 seconds
- Timestamps in milliseconds
- Indexed by timestamp for fast queries
- Automatic cleanup prevents bloat

---

## 🔧 Technical Architecture

### Project Structure
```
TempMonitor/
├── app/
│   ├── build.gradle           # Dependencies & build config
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/thermalmonitor/
│   │   │   ├── MainActivity.kt
│   │   │   ├── model/
│   │   │   │   └── TemperatureData.kt
│   │   │   ├── database/
│   │   │   │   ├── TemperatureDao.kt
│   │   │   │   └── TemperatureDatabase.kt
│   │   │   ├── service/
│   │   │   │   ├── FloatingWindowService.kt
│   │   │   │   ├── AlarmService.kt
│   │   │   │   ├── AlarmNotificationManager.kt
│   │   │   │   └── AlarmReceiver.kt
│   │   │   ├── ui/
│   │   │   │   ├── fragment/
│   │   │   │   │   ├── HomeFragment.kt
│   │   │   │   │   ├── HistoryFragment.kt
│   │   │   │   │   └── AlarmFragment.kt
│   │   │   │   └── adapter/
│   │   │   │       └── TabsPagerAdapter.kt
│   │   │   └── utils/
│   │   │       └── TemperatureUtils.kt
│   │   └── res/
│   │       ├── layout/
│   │       ├── values/
│   │       └── drawable/
│   └── proguard-rules.pro
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle
├── settings.gradle
├── gradle.properties
└── README.md
```

### Key Classes

#### MainActivity.kt
- Entry point of application
- Manages 3 tabs using ViewPager2
- Continuous temperature monitoring background thread
- Alarm checking every 5 seconds
- Permission handling for Android 13+ and overlay

#### HomeFragment.kt
- Real-time temperature display
- Progress bars and status
- Gradient background updates
- Floating window toggle control

#### HistoryFragment.kt
- Time range selection
- Line chart visualization
- Data fetching from Room database
- Flow-based reactive updates

#### AlarmFragment.kt
- Alarm type dropdown
- Threshold input with validation
- Set/Disable alarm controls
- Persistent settings storage

#### FloatingWindowService.kt
- Background service for overlay window
- Touch-to-move functionality
- Real-time temperature updates
- Gradient color application

#### AlarmService.kt
- Alarm state management
- SharedPreferences persistence
- Threshold checking logic
- Multiple simultaneous alarms

#### TemperatureUtils.kt
- CPU temperature reading from /sys/class/thermal/
- Battery temperature via BatteryManager
- Gradient color calculation
- Temperature formatting and validation

### Dependencies
```gradle
// Core Android
androidx.core:core-ktx:1.13.0
androidx.appcompat:appcompat:1.6.1

// UI Components
com.google.android.material:material:1.11.0
androidx.viewpager2:viewpager2:1.0.0
androidx.constraintlayout:constraintlayout:2.1.4

// Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// Lifecycle & Coroutines
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3

// Charts
com.github.PhilJay:MPAndroidChart:v3.1.0

// Kotlin
org.jetbrains.kotlin:kotlin-stdlib
```

---

## 🚀 Build & Deployment

### Build Configuration
```gradle
compileSdk 35
targetSdk 35
minSdk 21
jvmTarget = '17'
```

### Build Variants
- **Debug**: Development with full logging
- **Release**: Optimized with ProGuard minification

### APK Information
- Minimum Size: ~5 MB
- Compiled for: API 21-35
- Supported ABIs: armeabi-v7a, arm64-v8a, x86, x86_64

---

## 📋 User Guide

### First Launch
1. **Grant Permissions**: 
   - Tap "Allow" for floating window permission
   - Tap "Allow" for notification permission (if prompted)

2. **Homepage Usage**:
   - Monitor real-time temperatures
   - Toggle floating window on/off
   - Observe gradient color changes

3. **Set Alarm**:
   - Go to Alarm tab
   - Select CPU/Battery/Both
   - Enter temperature threshold
   - Tap "Set Alarm"

4. **View History**:
   - Go to History tab
   - Select time range (1hr/1day/1week)
   - View trend chart
   - Tap refresh to update

5. **Floating Window**:
   - Enable from Home tab
   - Drag to reposition
   - Disable when not needed

### Tips
- Keep app running for continuous monitoring
- Check history weekly for patterns
- Set reasonable alarm thresholds (35-60°C)
- Use floating window for quick reference

---

## 🔍 Temperature Thresholds

### Recommended Values
- **Normal**: < 30°C
- **Warm**: 30-40°C
- **Hot**: 40-50°C
- **Very Hot**: 50-60°C
- **Critical**: > 60°C

### Alarm Recommendations
- Conservative: 50°C
- Moderate: 55°C
- Aggressive: 60°C

---

## 📱 Device Compatibility

### Supported
- ✅ All Android devices with API 21+
- ✅ Devices with accessible thermal zones
- ✅ Devices with BatteryManager support

### Tested On
- Android 5.0 (API 21) - Minimum
- Android 16 (API 35) - Target
- Real devices with thermal files
- Emulators (with simulated temps)

---

## 🐛 Known Limitations

1. **Emulator**: Thermal readings may not be accurate
2. **Custom ROMs**: Thermal paths may differ
3. **Permissions**: Overlay requires explicit permission
4. **Database**: Limited to app's private storage
5. **Notifications**: Requires POST_NOTIFICATIONS permission

---

## 🔮 Future Enhancements

- [ ] GPU temperature monitoring
- [ ] Custom notification sounds
- [ ] Data export (CSV/JSON)
- [ ] Multiple alarm thresholds
- [ ] Home screen widget
- [ ] Background service optimization
- [ ] Statistics and analytics
- [ ] Cloud sync (optional)
- [ ] Dark mode theme
- [ ] Multi-language support

---

## 📞 Support & Troubleshooting

### Temperature Shows 0°C
- This is expected on emulator
- Test on real device

### Floating Window Not Showing
- Grant SYSTEM_ALERT_WINDOW permission
- Check Settings → Apps → Display over other apps

### Alarm Not Triggering
- Ensure alarm is enabled
- Verify threshold is set correctly
- Check notification permissions

### App Crashes
- Update to latest Android version
- Clear app cache
- Reinstall application

---

## 📄 License

This application is provided for educational and personal use.

---

## 📞 Contact & Support

For technical questions or bug reports, refer to the development documentation in SETUP.md

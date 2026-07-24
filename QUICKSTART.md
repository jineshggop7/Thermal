# Quick Start Guide - Thermal Monitor

## ⚡ 5-Minute Setup

### Step 1: Open Project
1. Launch **Android Studio**
2. File → Open → Select the project folder
3. Wait for Gradle sync (2-3 minutes)

### Step 2: Install SDK (if needed)
1. Tools → SDK Manager
2. Ensure **Android API 35** is installed
3. Click "Apply" and "OK"

### Step 3: Create/Select Device
**Option A - Emulator:**
1. Tools → Device Manager → Create Device
2. Select API 35 image
3. Click "Create"

**Option B - Physical Device:**
1. Enable Developer Mode (Settings → About → Tap Build 7 times)
2. Enable USB Debugging
3. Connect via USB cable

### Step 4: Run App
1. Select device in top toolbar
2. Click Run (▶️) or press **Shift+F10**
3. App launches with permissions prompt

### Step 5: Grant Permissions
- Tap "Allow" for floating window permission
- Tap "Allow" for notification permission (if prompted)

## 📱 First Steps in App

### Homepage (Tab 1)
- ✅ View real-time CPU & Battery temperatures
- ✅ Toggle floating window on
- ✅ Watch gradient background change color

### History (Tab 2)
- ✅ Select "1 Hour" time range
- ✅ View temperature trend chart
- ✅ Tap "Refresh" to update

### Alarm (Tab 3)
- ✅ Keep "CPU" selected
- ✅ Enter "55" as threshold
- ✅ Tap "Set Alarm"

## 🎯 Testing Features

### Real-Time Display
- Temperatures update every 1 second
- Progress bars fill based on temperature

### Floating Window
- Tap "Start Floating Window" on Home tab
- Window appears on screen
- Drag to reposition
- Tap "Stop Floating Window" to hide

### Alarm System
- Set threshold to 55°C
- Wait for temperature to reach threshold
- Notification appears with 5 beeps
- Device vibrates in pattern

### History Chart
- Go to History tab
- Chart shows temperature trends
- Switch time ranges (1hr/1day/1week)
- Pinch/zoom on chart

## 🔧 Build Commands

### Clean Build
```bash
cd /path/to/project
./gradlew clean
./gradlew build
```

### Debug Build
```bash
./gradlew assembleDebug
```

### Run on Device
```bash
./gradlew installDebug
```

## 🐛 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Gradle Sync Failed | Run `./gradlew clean` then refresh |
| Temperature Shows 25°C | Normal on emulator, test on real device |
| Floating Window Not Showing | Grant SYSTEM_ALERT_WINDOW permission |
| App Won't Run | Verify Android API 35 is installed |
| Build Takes Long | First build is slow, subsequent builds are faster |

## 📱 Files to Know

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Main app activity with 3 tabs |
| `HomeFragment.kt` | Real-time temperature display |
| `HistoryFragment.kt` | Temperature trend charts |
| `AlarmFragment.kt` | Alarm settings |
| `FloatingWindowService.kt` | Overlay window service |
| `TemperatureUtils.kt` | Temperature reading utilities |
| `TemperatureDatabase.kt` | Room database setup |

## 📚 Full Documentation

- **README.md** - Features overview
- **FEATURES.md** - Detailed feature documentation
- **SETUP.md** - Complete installation guide
- **PROJECT_SUMMARY.md** - Architecture & structure

## ✅ Verify Installation

After first run, check:
- [x] Temperatures display (not showing "--°C")
- [x] Progress bars move
- [x] Gradient background changes color
- [x] Floating window toggles
- [x] Alarm tab saves settings
- [x] History shows chart (after 5 minutes of data)

## 🚀 Next Steps

1. **Explore the Code**: Open files in Android Studio
2. **Modify Theme**: Edit colors in `values/colors.xml`
3. **Test on Real Device**: Connect physical Android phone
4. **Build Release**: Run `./gradlew assembleRelease`
5. **Deploy**: Share APK or upload to Play Store

## 💡 Tips

- App works best on Android 5.0+ devices
- Temperature readings need real thermal zones (not emulator)
- Keep app running for continuous monitoring
- Floating window uses minimal resources
- History stores data automatically every 5 seconds

## 📞 Need Help?

1. Check **SETUP.md** for detailed installation
2. Review **FEATURES.md** for feature details
3. Look at class comments in source files
4. Check Android Studio's Logcat for errors

---

**Status**: ✅ Ready to Use
**Estimated Setup Time**: 5-10 minutes
**All Features**: Implemented & Working

Enjoy your Thermal Monitor app! 🎉

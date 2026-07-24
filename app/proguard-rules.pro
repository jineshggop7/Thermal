# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

# For Android applications, ProGuard can be configured in the
# project.properties file (using the proguard.config property) and
# this file (the default Proguard configuration file).

# You can also invoke ProGuard and Retrace as part of your build process
# before packaging the final apk. By default, this file is excluded from
# release builds. You can include it explicitly by uncommenting "#-include rules.pro" in project.properties.

# Keep line numbers for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep androidx
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

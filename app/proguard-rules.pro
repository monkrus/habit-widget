# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep data classes
-keep class com.habitstreak.app.data.** { *; }

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Google Play Billing classes
-keep class com.android.billingclient.** { *; }

# Keep widget receiver
-keep class com.habitstreak.app.widget.HabitWidgetReceiver { *; }
-keep class com.habitstreak.app.receiver.BootReceiver { *; }

# Keep BillingManager
-keep class com.habitstreak.app.billing.BillingManager { *; }

# DataStore
-keep class androidx.datastore.*.** {*;}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep Compose
-keep class androidx.compose.** { *; }
-keep class androidx.activity.ComponentActivity { *; }

# Keep @Composable functions
-keep @androidx.compose.runtime.Composable class * { *; }
-keep class * {
    @androidx.compose.runtime.Composable *;
}

# --- Hilt ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- Broadcast receivers (manifest-instantiated) ---
-keep class com.app.notifications.data.receiver.** { *; }

# --- Notification payload extras used from PendingIntent ---
-keepclassmembers class com.app.notifications.MainActivity {
    public void onCreate(android.os.Bundle);
}

# --- Coroutines (avoid over-shrinking dispatcher internals) ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# --- Parcelable / Serializable models if added later ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

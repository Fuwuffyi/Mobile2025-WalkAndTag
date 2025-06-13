# Keep Compose runtime and snapshots
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.runtime.snapshots.** { *; }

# Keep Composable functions and their previews
-keepclasseswithmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep ViewModel constructors used in Compose (e.g. Hilt)
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Keep coroutine state handling classes
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# Remove verbose Log calls in production
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int v(...);
    public static int println(...);
}
# Keep the results screen classes that are restored via reflection
-keep class com.android.developers.androidify.results.** { *; }

# Keep Compose, to work around a release-only crash on the results screen
-keep class androidx.compose.** { *; }

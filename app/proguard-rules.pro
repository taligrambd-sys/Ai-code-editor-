# R8 / ProGuard Configuration for DeepSeek AI Web Code Studio

# Preserve Kotlin Reflection / Serialized attributes for Gson
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations

# Keep Gson Serialized Names and Data Models
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Data Models in app package
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** { *; }
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }

# Retrofit
-keepattributes *Annotation*
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# WebView JavaScript Interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Compose view classes
-keep class androidx.compose.** { *; }

# Line numbers for debug traces
-keepattributes SourceFile,LineNumberTable

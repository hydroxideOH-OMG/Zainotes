# ProGuard rules for Zainotes
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Database *;
    @androidx.room.Entity *;
}
-keep class com.zainotes.app.data.model.** { *; }
-keep class com.zainotes.app.data.preferences.** { *; }
-keep class com.zainotes.app.data.ai.** { *; }

# Gson preservation
-keepattributes Signature, *Annotation*, EnclosingMethod
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Coroutines and AndroidX
-dontwarn java.lang.invoke.MethodHandles$Lookup
-dontwarn okio.**
-dontwarn javax.annotation.**

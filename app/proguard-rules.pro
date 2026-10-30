# ProGuard rules for Zainotes
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Database *;
    @androidx.room.Entity *;
}
-keep class com.zainotes.app.data.model.** { *; }
-dontwarn java.lang.invoke.MethodHandles$Lookup

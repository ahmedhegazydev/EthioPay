# Compose / Kotlin metadata (احتياطي)
-keep class kotlin.Metadata { *; }

# Stripe
-keep class com.stripe.android.** { *; }
-dontwarn com.stripe.android.**

# Firebase / Play services (غالباً مش محتاج keep كتير، بس ده يقلل warnings)
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Scanbot (مهم جداً)
-keep class io.scanbot.** { *; }
-dontwarn io.scanbot.**

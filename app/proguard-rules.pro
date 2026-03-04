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


-keep public class com.inet.facelock.callback.FaceCallback {*;}
-keep public class com.inet.facelock.callback.FaceDetectionResult {*;}

# iText 5 "unified" events refer to iText 7 commons actions (optional)
-dontwarn com.itextpdf.commons.actions.**
-dontwarn com.itextpdf.commons.actions.confirmations.**
-dontwarn com.itextpdf.commons.actions.contexts.**
-dontwarn com.itextpdf.commons.actions.data.**
-dontwarn com.itextpdf.commons.actions.processors.**
-dontwarn com.itextpdf.commons.actions.producer.**
-dontwarn com.itextpdf.commons.actions.sequence.**

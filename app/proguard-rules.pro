# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

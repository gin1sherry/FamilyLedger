# Keep AI / network models
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.example.familyledger.data.model.** { *; }
-keep class com.example.familyledger.data.remote.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**

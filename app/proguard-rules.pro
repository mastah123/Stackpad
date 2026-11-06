# Stackpad release shrinking rules.

# kotlinx.serialization: keep generated serializers for the block model.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.stackpad.app.**$$serializer { *; }
-keepclassmembers class com.stackpad.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.stackpad.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

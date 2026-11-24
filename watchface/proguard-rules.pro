# Ignore missing Java SE classes from TwelveMonkeys ImageIO
-dontwarn javax.imageio.**

# Ignore missing Java SE classes from XML libraries (Xerces, etc.)
-dontwarn org.apache.xml.resolver.**
-dontwarn org.eclipse.wst.xml.xpath2.processor.**

# Ignore missing Java SE annotation processing classes, often from libraries like AutoValue/JavaPoet
-dontwarn javax.lang.model.**

-keep class com.android.developers.androidify.watchface.creator.PackPackage {
    native <methods>;
}

-keep class com.android.developers.androidify.watchface.creator.PackPackage$Resource { *; }

# Keep all classes in the BouncyCastle provider, as they are loaded via reflection
-keep class org.bouncycastle.** { *; }
-keep interface org.bouncycastle.** { *; }

# Keep the APK Signer library
-keep class com.android.apksig.** { *; }
-keep interface com.android.apksig.** { *; }

# Keep Apache Xerces XML parser
-keep class org.apache.xerces.** { *; }

## Keep standard Java XML (JAXP), DOM, and SAX interfaces and classes
-keep interface org.w3c.dom.** { *; }
-keep class org.w3c.dom.** { *; }
-keep interface org.xml.sax.** { *; }
-keep class org.xml.sax.** { *; }
-keep class javax.xml.** { *; }
-keep interface javax.xml.** { *; }

# Keep the watch face classes that are loaded via reflection
-keep class com.android.developers.androidify.watchface.** { *; }

# Ignore missing Java SE classes from TwelveMonkeys ImageIO
-dontwarn javax.imageio.**

# Ignore missing Java SE classes from XML libraries (Xerces, etc.)
-dontwarn org.apache.xml.resolver.**
-dontwarn org.eclipse.wst.xml.xpath2.processor.**

# Ignore missing Java SE annotation processing classes, often from libraries like AutoValue/JavaPoet
-dontwarn javax.lang.model.**
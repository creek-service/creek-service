[![javadoc](https://javadoc.io/badge2/org.creekservice/creek-service-extension/javadoc.svg)](https://javadoc.io/doc/org.creekservice/creek-service-extension)

# Extension

Module containing the types needed to implement an extension to handle resources in Creek.

The types of resources that Creek can understand can be extended by adding extensions. 
An extension can be registered by adding a jar to the class or module path that exposes a service 
implementation of `CreekExtensionProvider`. 

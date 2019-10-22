# XNAT 1.7 CollectionPlugin #

This is the XNAT 1.7 Collection Plugin. It helps you create and manage data collections.

# Building #

To build the XNAT 1.7 collection plugin:

1. If you haven't already, clone this repository and cd to the newly cloned folder.
1. Build the plugin: `./gradlew clean build`. This should build the plugin in the file **build/libs/xnat-collection-plugin-1.0.0.jar** (the version may differ based on updates to the code).
1. Copy the plugin jar to your plugins folder: `cp build/libs/xnat-collection-plugin-1.0.0.jar /data/xnat/home/plugins`
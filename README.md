# XNAT Datasets Plugin #

This is the XNAT Datasets Plugin. It helps you create and manage dataset definitions and resolved datasets.

# Building #

To build the XNAT Datasets Plugin:

1. If you haven't already, clone this repository and cd to the newly cloned folder.
1. Build the plugin: `./gradlew clean build`. This should build the plugin in the file **build/libs/datasets-plugin-1.8.0.jar** (the version may differ based on updates to the code).
1. Copy the plugin jar to your plugins folder: `cp build/libs/datasets-plugin-1.0.0.jar /data/xnat/home/plugins`
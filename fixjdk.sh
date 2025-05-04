#!/bin/bash

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "Java home set to: $JAVA_HOME"
java -version

# Remove any corrupt Gradle cache
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/daemon/
rm -rf ~/.gradle/native/

# Clean and rebuild
./gradlew clean
./gradlew assembleDebug 
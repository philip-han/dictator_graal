# Introduction
This is a sample library component for Android and iOS mobile apps. Please refer to [companion article](https://medium.com/@philip.han_66842/graalvm-native-image-for-mobile-development-49eb87a00eee) for more information.

[Android App](https://github.com/philip-han/Great-Dictator-Android)

[iOS App](https://github.com/philip-han/Great-Dictator-iOS)

This module depends on `speechcore` module found in Android App, so please configure and build it first.

<br>

## Instruction
Please create `local.properties` in your project root prior to building.

```
staticJdk=<location of static JDK>
temp=<location of tmp directory>
speechcoreModule=<location of speechcore module>
```
Note that `speechcore` module is located in a different project root. You'll find it under [Android App](https://github.com/philip-han/Great-Dictator-Android).

<br>

## Tooling
Please install [GraalVM Tools for Java - Visual Studio Marketplace](https://marketplace.visualstudio.com/items?itemName=oracle-labs-graalvm.graalvm) first. You will need following modules once the extension is installed. You may also download and install GraalVM using this extension.

![GraalVM Extension Modules](/images/graalvm_plugin_modules.png)

Tested GraalVM version is 22.1.0 or lower. Higher version is not working at the moment due to modules not importing correctly. Will update as soon as that is working.

<br>

## Downloading Static JDK for ios-aarch64 and ios-x86_64
You may obtain the static JDK libs by first cloning Gluon Samples. 

[Gluon Samples Repository](https://github.com/gluonhq/gluon-samples)

Change to `HelloFX` directory and execute this command:

`mvn -Pios gluonfx:compile`

Once this command completes, you will have the static libs under `<USER_HOME>/.gluon/substrate/javaStaticSdk/18-ea+prep18-8/ios-x86_64/staticjdk/lib/static`

You may need to adjust the path depending on which version was downloaded, .e.g. 18-ea+prep18-8.

Use this path to fill in the properties file above.

<br>

## Building
From the project root, build and run `app` module with the tracing agent enabled, then native compilation:
```
./gradlew app:run -Pagent
./gradlew gradle_interface:nativeCompile
```

Currently, VS Code Gradle extension is not working properly on this project. In order to make it work, you'll have to hard code the path of `speechcore` module. Details are at the bottom of `settings.gradle` file. So, use either commands above or enable/fix the Gradle extension.

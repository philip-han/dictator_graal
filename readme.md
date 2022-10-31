# Instruction
Please create `local.properties` in your project root prior to building.

```
staticJdk=<location of static JDK>
temp=<location of tmp directory>
```

## Downloading Static JDK for ios-aarch64 and ios-x86_64
You may obtain the static JDK libs by first cloning Gluon Samples. 

[Gluon Samples Repository](https://github.com/gluonhq/gluon-samples)

Change to `HelloFX` directory and execute this Gradle command:

`mvn -Pios gluonfx:compile`

Once this command completes, you will have the static libs under `<USER_HOME>/.gluon/substrate/javaStaticSdk/18-ea+prep18-8/ios-x86_64/staticjdk/lib/static`

You may need to adjust the path depending on which version was downloaded, .e.g. 18-ea+prep18-8.

Use this path to fill in the properties file above.
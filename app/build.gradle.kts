plugins {
    id("com.android.application")
}

android {
    namespace = "edu.astate.siddarth.tidalrelay"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.astate.siddarth.tidalrelay"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

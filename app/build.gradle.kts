plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "app.hubhome.launcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.hubhome.launcher"
        minSdk = 23
        // الجهاز المستهدف يعمل بـ Android 12 (API 31)
        targetSdk = 31
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // توقيع بمفتاح debug ليكون ملف release قابلًا للتثبيت مباشرة (sideload).
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // بلا اعتماديات خارجية عمدًا: Android framework + Kotlin stdlib فقط (أخف وأسرع على 1GB RAM).
}

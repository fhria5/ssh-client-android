plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sshclient"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sshclient"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packagingOptions {
        pickFirst("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
        pickFirst("META-INF/versions/11/OSGI-INF/MANIFEST.MF")
        pickFirst("META-INF/versions/15/OSGI-INF/MANIFEST.MF")
        pickFirst("META-INF/versions/21/OSGI-INF/MANIFEST.MF")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // SSHJ only (hapus osgi + jediterm + slf4j)
    implementation("com.hierynomus:sshj:0.39.0") {
        exclude(group = "org.bouncycastle", module = "bcpkix-jdk18on")
        exclude(group = "org.bouncycastle", module = "bcprov-jdk18on")
        exclude(group = "org.bouncycastle", module = "bcutil-jdk18on")
    }

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

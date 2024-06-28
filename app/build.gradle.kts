plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.nt118.proma"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nt118.proma"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }

    dataBinding {
        enable = true
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
    }
}
dependencies {
    implementation("com.facebook.android:facebook-android-sdk:[4,5)")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.legacy.support.v4)
    implementation(libs.activity)
    implementation(libs.gridlayout)
    implementation(libs.datastore.core.android)
    implementation(libs.work.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.github.AnyChart:AnyChart-Android:1.1.5")
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("com.akexorcist:snap-time-picker:1.0.3")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.1.0")
    implementation("com.facebook.android:facebook-android-sdk:12.3.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.firebase:firebase-storage")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.firebase:firebase-messaging:23.0.2")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.android.volley:volley:1.2.1")
}
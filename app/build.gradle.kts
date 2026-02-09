


//val implementation: Unit

plugins {
    //alias(libs.plugins.android.application)

    id("com.android.application")
}

android {
    namespace = "com.smartpdfsuite"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smartpdfsuite"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// ... Latest code_cursor ...

dependencies {
    // AndroidX Core & UI

//    implementation("androidx.core:core:1.12.0")
//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation 'com.google.android.material:material:1.11.0'
//    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
//    implementation 'androidx.activity:activity:1.8.0'
//
//
//    implementation 'androidx.fragment:fragment:1.6.0'
//
//    // Navigation Component (required for the mobile_navigation.xml)
//    implementation 'androidx.navigation:navigation-fragment:2.7.7'
//    implementation 'androidx.navigation:navigation-ui:2.7.7'
//
//    // Lifecycle components
//    implementation "androidx.lifecycle:lifecycle-viewmodel:2.6.1"
//    implementation "androidx.lifecycle:lifecycle-livedata:2.6.1"
//    implementation "androidx.lifecycle:lifecycle-runtime:2.6.1"
//
//    // Room database
//    implementation "androidx.room:room-runtime:2.6.1"
//    annotationProcessor "androidx.room:room-compiler:2.6.1"
//
//    // PDF Viewer Library (Android PdfViewer)
//    implementation 'com.github.barteksc:android-pdf-viewer:3.2.0-beta.1'
//
//    // Image loading library (for PDF Maker from images)
//    implementation 'com.github.bumptech.glide:glide:4.16.0'
//    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'

//    // CameraX for PDF Scanner
//    implementation 'androidx.camera:camera-core:1.3.1'
//    implementation 'androidx.camera:camera-camera2:1.3.1'
//    implementation 'androidx.camera:camera-lifecycle:1.3.1'
//    implementation 'androidx.camera:camera-view:1.3.1'
//    implementation 'androidx.camera:camera-extensions:1.3.1' // For additional camera features
//
//    // Google ML Kit for Text Recognition (optional, for OCR in scanner)
//    // Note: These are beta versions; ensure compatibility if used in production.
//    implementation 'com.google.mlkit:text-recognition:16.0.0-beta6'
//    implementation 'com.google.mlkit:text-recognition-latin:16.0.0-beta6'
//
//    // Test dependencies
//    testImplementation 'junit:junit:4.13.2'
//    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
//    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'


/* implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")

    implementation("androidx.fragment:fragment:1.6.0")

// Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

// Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.1")

// Room database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

// PDF Viewer Library

//    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.1")


// Glide Image Loader
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

// CameraX for PDF Scanner
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-extensions:1.3.1")

// Google ML Kit for OCR
//    implementation("com.google.mlkit:text-recognition:16.0.0-beta6")
//    implementation("com.google.mlkit:text-recognition-latin:16.0.0-beta6")
    implementation("com.google.mlkit:text-recognition-latin:16.0.0")
//    implementation("com.google.mlkit:text-recognition-digital:16.0.0")


// Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
*/






//11_12_2025: 4-30 pm
    //
    //implementation 'com.github.barteksc:android-pdf-viewer:3.2.0-beta.1'


    //
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.fragment:fragment:1.6.0")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.1")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // PDF Viewer (Correct Maintained Version)
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.1")

    // Glide Image Loader
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // CameraX for PDF Scanner
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-extensions:1.3.1")

    // Google ML Kit OCR (Correct Versions)

//    implementation("com.google.mlkit:text-recognition-latin:16.0.0-beta6")
    implementation("com.google.mlkit:text-recognition:16.0.0-beta6")

   // implementation("com.google.mlkit:text-recognition-latin:16.0.0")
   // implementation("com.google.mlkit:text-recognition-digital:16.0.0")

    // Test Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

// for convert img to pdf


    implementation("com.itextpdf:itextg:5.5.10")

}

// ... Default code...

//dependencies {
//
//    implementation(libs.appcompat)
//    implementation(libs.material)
//    implementation(libs.activity)
//    implementation(libs.constraintlayout)
//    implementation(libs.navigation.fragment)
//    implementation(libs.navigation.ui)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.ext.junit)
//    androidTestImplementation(libs.espresso.core)

//}

/*
===========================

plugins {
    id("com.android.application")
    kotlin("android") version "1.9.0" apply false
}

android {
    namespace = "com.smartpdfsuite"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smartpdfsuite"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // ===============================
    // CORE AND UI
    // ===============================
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.fragment:fragment:1.6.0")

    // ===============================
    // NAVIGATION COMPONENT
    // ===============================
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // ===============================
    // LIFECYCLE
    // ===============================
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.1")

    // ===============================
    // ROOM DATABASE
    // ===============================
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // ===============================
    // PDF VIEWER
    // ===============================
    //implementation("com.github.barteksc:android-pdf-viewer:3.1.0")
   // implementation("com.github.barteksc:android-pdf-viewer:2.8.2")
    implementation("com.github.barteksc:android-pdf-viewer:2.8.1")

    // ===============================
    // IMAGE LOADER
    // ===============================
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ===============================
    // CAMERAX (PDF SCANNER)
    // ===============================
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-extensions:1.3.1")

    // ===============================
    // ML KIT OCR
    // ===============================
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // ===============================
    // IMAGE → PDF
    // ===============================
    implementation("com.itextpdf:itextg:5.5.10")

    // ===============================
    // TESTING
    // ===============================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}*/

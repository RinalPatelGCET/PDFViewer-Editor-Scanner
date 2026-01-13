plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.pdfviewer_editor_scanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pdfviewer_editor_scanner"
        minSdk = 24
        targetSdk = 35
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
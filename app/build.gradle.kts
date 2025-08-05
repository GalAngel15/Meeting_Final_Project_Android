plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.meeting_project"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.meeting_project"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.firebase:firebase-auth:22.0.0")
    implementation (libs.gson)
    implementation(project(":chatlibrary"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Json parser
    implementation(libs.gson.v2101)

    // Rest API calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation ("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database)
    implementation(libs.google.firebase.auth)
//    implementation(libs.androidsvg.aar)
    implementation (libs.firebase.storage)
    implementation (libs.circleimageview)
    implementation (libs.androidsvg)
//    implementation (libs.com.firebaseui.firebase.ui.auth)
//    implementation (libs.github.glide)
//    annotationProcessor (libs.glide.compiler)
//    implementation(platform(libs.google.firebase.bom))
    //location
    implementation (libs.play.services.maps)
    implementation (libs.play.services.location)
}
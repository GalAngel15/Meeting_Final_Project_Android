plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.meeting_project"
    compileSdk = 35

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
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Json parser
    implementation(libs.gson.v2101)

    // Rest API calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // firebase
    implementation(platform(libs.firebase.bom.v3320))
    implementation (libs.firebase.messaging)
    implementation ("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database)
    implementation(libs.google.firebase.auth)
    implementation (libs.firebase.storage)

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    implementation (libs.circleimageview)
    implementation (libs.androidsvg)

    //location
    implementation (libs.play.services.maps)
    implementation (libs.play.services.location)

    // Retrofit

    implementation (libs.logging.interceptor.v4120)

    implementation (libs.converter.scalars)
    implementation (libs.core.ktx) // או Java, לא חובה


}
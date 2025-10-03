plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.smarthomeui"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smarthomeui"
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

dependencies {
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //api
    dependencies {
        implementation ("com.squareup.retrofit2:retrofit:2.11.0")
        implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
        implementation ("com.squareup.okhttp3:okhttp:4.12.0")
        implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
        implementation ("androidx.lifecycle:lifecycle-livedata:2.8.4")
        implementation ("androidx.lifecycle:lifecycle-viewmodel:2.8.4")
    }

}
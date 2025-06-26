plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"



}

android {
    namespace = "com.youcef_bounaas.athlo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.youcef_bounaas.athlo"
        minSdk = 26
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)





        // Jetpack Compose
        implementation("androidx.compose.ui:ui:1.4.0")
        implementation("androidx.compose.material:material:1.4.0")
        implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")

        // ViewModel & LiveData
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")


    // MAPBOX
     implementation("com.mapbox.maps:android:11.10.3")
    implementation("com.mapbox.extension:maps-compose:11.10.3")
    implementation("com.mapbox.plugin:maps-locationcomponent:11.10.3")












    // Room Database
        implementation("androidx.room:room-runtime:2.4.3")
        kapt("androidx.room:room-compiler:2.4.3")
        implementation("androidx.room:room-ktx:2.4.3")

        // Retrofit (API Calls)
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")



        // Coroutines & Flow
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")




        // Google Sensors API
        implementation("com.google.android.gms:play-services-fitness:20.0.0")

        // Navigation
        implementation("androidx.navigation:navigation-compose:2.5.3")


    //Koin
        implementation ("io.insert-koin:koin-android:3.5.0")
        implementation ("io.insert-koin:koin-core:3.5.0")
        implementation ("io.insert-koin:koin-androidx-compose:3.5.0" )

       // Extended icons
       implementation ("androidx.compose.material:material-icons-extended:1.7.8")


//responsiveness
    implementation ("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation ("androidx.compose.material3:material3-window-size-class:1.1.0")

    // type safe navigation
    implementation ("androidx.navigation:navigation-compose:2.7.7")



  // HorizontalPager for swiping

    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")




    implementation ("androidx.compose.material:material:1.7.8") // or the latest version
    implementation ("androidx.compose.ui:ui:1.5.4") // or the latest version



    //Supabase

    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt-android")


// Ktor for Android networking
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("io.ktor:ktor-client-okhttp:3.0.0-rc-1")


    





}



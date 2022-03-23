plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = appVersion.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bitmart.demo"
        minSdk = appVersion.versions.minSdk.get().toInt()
        targetSdk = appVersion.versions.targetSdk.get().toInt()
        versionCode = appVersion.versions.versionCode.get().toInt()
        versionName = appVersion.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testApplicationId = "com.bitmart.demo.test"

        multiDexEnabled = true
    }

    testBuildType = "debug"

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            isJniDebuggable = true
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(project(":data:data-local"))
    implementation(project(":data:data-remote"))
    implementation(project(":bitmartchart"))

    implementation(jetpack.core)
    implementation(jetpack.room)
    implementation(jetpack.appcompat)
    implementation(jetpack.material)
    implementation(jetpack.viewmodel)
    implementation(jetpack.lifecycle)
    implementation(jetpack.activity)
    implementation(jetpack.constraintlayout)

    implementation(third.koin)
    implementation(third.retrofit)
    implementation(third.gson)
    implementation(third.gsonConverter)
    implementation(third.retrofitLogger)

    testImplementation(test.room)
    testImplementation(test.koin)
    testImplementation(test.junit)
    testImplementation(test.mockito)
    testImplementation(test.koinJunit)
    testImplementation(test.coroutines)
    testImplementation(test.mockWebServer)

    androidTestImplementation(test.androidTest)
    androidTestImplementation(test.androidTestCore)
    androidTestImplementation(test.androidTestTruth)
    androidTestImplementation(test.androidTestRunner)
    androidTestImplementation(test.androidTestEspresso)

    androidTestUtil(test.androidTestUtil)

}
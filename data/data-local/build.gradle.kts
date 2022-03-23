plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    compileSdk = appVersion.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = appVersion.versions.minSdk.get().toInt()
        targetSdk = appVersion.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    api(project(":data:data-domain"))

    implementation(jetpack.core)

    implementation(jetpack.room)
    implementation(jetpack.roomKtx)
    implementation(jetpack.roomPaging)
    kapt(jetpack.roomCompiler)

    implementation(third.gson)
    implementation(third.koin)
    implementation(third.coroutines)

    testImplementation(test.junit)
    testImplementation(test.room)
    testImplementation(test.coroutines)
    androidTestImplementation(test.androidTest)
    androidTestImplementation(test.androidTestEspresso)

}
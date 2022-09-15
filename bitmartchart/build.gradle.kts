plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(third.coroutines)

    implementation(jetpack.core)
    implementation(jetpack.appcompat)
    implementation(jetpack.material)

    testImplementation(test.junit)
    testImplementation(test.coroutines)

    androidTestImplementation(test.androidTest)
    androidTestImplementation(test.androidTestEspresso)
}

afterEvaluate {
    publishing {
        publications {

            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.bitmart.android"
                artifactId = "k-chart"
                version = "1.2.0"
            }

            repositories {
                maven {
                    isAllowInsecureProtocol = true
                    url = uri("http://nexus.bitmartpro.com/repository/maven-releases/")
                    credentials {
                        username = "m2_push"
                        password = "8V@P6F47OH3!"
                    }
                }
            }
        }
    }
}

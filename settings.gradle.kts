pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

}
enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {

        val roomVersion = "2.4.1"

        val koinVersion = "3.2.0-beta-1"

        val coroutinesVersion = "1.6.0"

        create("appVersion") {
            version("compileSdk", "31")
            version("targetSdk", "31")
            version("minSdk", "21")
            version("versionCode", "1")
            version("versionName", "0.0.1")
        }

        create("test") {
            alias("junit").to("junit:junit:4.13.2")
            alias("androidTest").to("androidx.test.ext:junit-ktx:1.1.3")
            alias("androidTestCore").to("androidx.test:core-ktx:1.4.0")
            alias("androidTestTruth").to("androidx.test.ext:truth:1.4.0")
            alias("androidTestRunner").to("androidx.test:runner:1.4.0")
            alias("androidTestEspresso").to("androidx.test.espresso:espresso-core:3.4.0")

            alias("room").to("androidx.room:room-testing:$roomVersion")
            alias("coroutines").to("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
            alias("koin").to("io.insert-koin:koin-test:$koinVersion")
            alias("koinJunit").to("io.insert-koin:koin-test-junit4:$koinVersion")
            alias("mockWebServer").to("com.squareup.okhttp3:mockwebserver:4.9.3")
            alias("mockito").to("org.mockito.kotlin:mockito-kotlin:4.0.0")

            alias("androidTestUtil").to("androidx.test:orchestrator:1.4.1")

        }

        create("third") {
            alias("koin").to("io.insert-koin:koin-android:$koinVersion")
            alias("coroutines").to("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            alias("coroutinesAndroid").to("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
            alias("retrofit").to("com.squareup.retrofit2:retrofit:2.9.0")
            alias("leakCanary").to("com.squareup.leakcanary:leakcanary-android:2.0.0")
            alias("gson").to("com.google.code.gson:gson:2.9.0")
            alias("gsonConverter").to("com.squareup.retrofit2:converter-gson:2.9.0")
            alias("retrofitLogger").to("com.squareup.okhttp3:logging-interceptor:4.9.0")

        }

        create("jetpack") {
            alias("core").to("androidx.core:core-ktx:1.7.0")
            alias("annotation").to("androidx.annotation:annotation:1.3.0")
            alias("appcompat").to("androidx.appcompat:appcompat:1.4.1")
            alias("material").to("com.google.android.material:material:1.5.0")
            alias("activity").to("androidx.activity:activity-ktx:1.4.0")
            alias("viewmodel").to("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
            alias("lifecycle").to("androidx.lifecycle:lifecycle-runtime-ktx:2.5.0")
            alias("room").to("androidx.room:room-runtime:$roomVersion")
            alias("roomKtx").to("androidx.room:room-ktx:$roomVersion")
            alias("roomPaging").to("androidx.room:room-paging:$roomVersion")
            alias("roomCompiler").to("androidx.room:room-compiler:$roomVersion")
            alias("constraintlayout").to("androidx.constraintlayout:constraintlayout:2.1.3")

        }
    }

}
rootProject.name = "android-kchart"
include(":app")
include(":data:data-remote")
include(":data:data-local")
include(":data:data-domain")
include(":bitmartchart")

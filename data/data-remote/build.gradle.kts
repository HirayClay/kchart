plugins {
    id("java-library")
    id("kotlin")
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


dependencies {

    api(project(":data:data-domain"))

    implementation(third.retrofit)
    implementation(third.coroutines)
    implementation(jetpack.annotation)

    testImplementation(test.junit)
    testImplementation(test.coroutines)
}
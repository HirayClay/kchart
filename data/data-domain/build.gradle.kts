plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies{

    implementation(third.coroutines)

    testImplementation(test.junit)
    testImplementation(test.coroutines)
}
import java.io.FileInputStream
import java.util.Properties
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

fun requireProp(key: String): String {
    return localProperties.getProperty(key)
        ?: throw GradleException("$key not set in local.properties — lihat petunjuk di README")
}

val appwriteEndpoint = requireProp("appwrite.endpoint")
val appwriteProjectId = requireProp("appwrite.project.id")
val appwriteDatabaseId = requireProp("appwrite.database.id")
val appwriteCollectionAnimals = requireProp("appwrite.collection.animals")
val appwriteBucketId = requireProp("appwrite.bucket.id")

android {
    namespace = "com.yarsi.rescuepet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yarsi.rescuepet"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "APPWRITE_ENDPOINT", "\"${appwriteEndpoint}\"")
        buildConfigField("String", "APPWRITE_PROJECT_ID", "\"${appwriteProjectId}\"")
        buildConfigField("String", "APPWRITE_DATABASE_ID", "\"${appwriteDatabaseId}\"")
        buildConfigField("String", "APPWRITE_COLLECTION_ANIMALS", "\"${appwriteCollectionAnimals}\"")
        buildConfigField("String", "APPWRITE_BUCKET_ID", "\"${appwriteBucketId}\"")

        manifestPlaceholders["appwriteProjectId"] = appwriteProjectId
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Appwrite SDK
    implementation("io.appwrite:sdk-for-android:6.0.0")

    // ViewModel & LiveData (MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Coroutines (async, dibutuhkan Appwrite)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Coil (load image di Compose)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // Google Location (GPS)
    implementation("com.google.android.gms:play-services-location:21.1.0")

}
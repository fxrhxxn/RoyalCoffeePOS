plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    /**
     * KSP is used by Room to generate database code at compile time.
     *
     * The plugin version is controlled from libs.versions.toml.
     */
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.royalcoffee.pos"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.royalcoffee.pos"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    /**
     * Material icons.
     */
    implementation("androidx.compose.material:material-icons-extended")

    /**
     * Room database dependencies.
     *
     * room-runtime:
     * Core Room database APIs.
     *
     * room-ktx:
     * Kotlin and coroutine support for Room.
     *
     * room-compiler:
     * Generates Room database code through KSP.
     */
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
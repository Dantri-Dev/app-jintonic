import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    // Apple Watch targets (device + simulators)
    listOf(
        watchosArm64(),
        watchosSimulatorArm64(),
        watchosX64()
    ).forEach { watchTarget ->
        watchTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    android {
        namespace = "com.jintonic.app.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Shared business logic dependencies go here.
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

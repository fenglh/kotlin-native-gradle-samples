import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask

plugins {
    kotlin("multiplatform") version "1.3.50"
}

repositories {
    jcenter()
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
    maven { setUrl("https://dl.bintray.com/kotlin/ktor") }
}

val ktorVersion = "1.2.5"

kotlin {
    sourceSets["commonMain"].dependencies {
        implementation(kotlin("stdlib-common"))
        implementation("io.ktor:ktor-client-core:$ktorVersion")
    }

    /*sourceSets {
        val commonMain by getting
        val ios32 by creating {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
    }*/

    configure(sourceSets.filter { it.name.startsWith("ios") }) {
        dependencies {
            implementation("io.ktor:ktor-client-ios:$ktorVersion")
            implementation("io.ktor:ktor-client-core-native:$ktorVersion")
        }
    }

    val ios32 = iosArm32("ios32")
    val ios64 = iosArm64("ios64")
    val iosSim = iosX64("iosSim")

    // If an initial framework has a custom base name, the fat framework must have the same base name.
    // See https://youtrack.jetbrains.com/issue/KT-30805.
    val frameworkName = "my_framework"

    configure(listOf(ios32, ios64, iosSim)) {
        binaries.framework {
            baseName = frameworkName
        }
    }

    tasks.create("debugFatFramework", FatFrameworkTask::class) {
        println("DEBUG FAT FRAMEWORK")
        baseName = frameworkName
        from(
                ios32.binaries.getFramework("DEBUG"),
                ios64.binaries.getFramework("DEBUG"),
                iosSim.binaries.getFramework("DEBUG")
        )
        destinationDir = buildDir.resolve("fat-framework/debug")
        group = "Universal framework"
        description = "Builds a universal (fat) debug framework"
    }


    tasks.create("releaseFatFramework", FatFrameworkTask::class) {
        baseName = frameworkName
        from(
                ios32.binaries.getFramework("RELEASE"),
                ios64.binaries.getFramework("RELEASE"),
                iosSim.binaries.getFramework("RELEASE")
        )
        destinationDir = buildDir.resolve("fat-framework/release")
        group = "Universal framework"
        description = "Builds a universal (fat) release framework"
    }
}

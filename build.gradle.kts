
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("multiplatform") version "2.3.21"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
}

tasks.withType(Detekt::class.java).configureEach {
    reports {
        html.required.set(true)
        sarif.required.set(true)
        txt.required.set(true)
        xml.required.set(false)
        md.required.set(false)
    }
}

ktlint {
    version.set("1.5.0")
    verbose.set(true)
    outputToConsole.set(true)
    enableExperimentalRules.set(true)
    filter {
        exclude("**/generated/**")
    }
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/stable")
}

kotlin {
    jvm()

    linuxX64 {
        binaries.executable {
            baseName = "Ofis"
            entryPoint = "ofis.platform.main"
        }
    }
    macosArm64 {
        binaries.executable {
            baseName = "Ofis"
            entryPoint = "ofis.platform.main"
        }
    }
    mingwX64 {
        binaries.executable {
            baseName = "Ofis"
            entryPoint = "ofis.platform.main"
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlinx.cinterop.BetaInteropApi")
        }
        val commonMain by getting {
            kotlin.srcDirs("src/common/kotlin")
            dependencies {
                implementation("com.squareup.okio:okio:3.18.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.uiToolingPreview)
            }
        }
        val commonTest by getting {
            kotlin.srcDirs("tests/common/kotlin")
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependsOn(commonMain)
            kotlin.srcDirs("src/jvm/kotlin")
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            kotlin.srcDirs("tests/jvm/kotlin")
            dependencies {
                implementation(kotlin("reflect"))
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
            kotlin.srcDirs("src/native/kotlin")
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }

        val macosMain by creating {
            dependsOn(nativeMain)
            kotlin.srcDirs("src/macos/kotlin")
        }
        val linuxMain by creating {
            dependsOn(nativeMain)
            kotlin.srcDirs("src/linux/kotlin")
        }
        val windowsMain by creating {
            dependsOn(nativeMain)
            kotlin.srcDirs("src/windows/kotlin")
        }

        getByName("linuxX64Main") { dependsOn(linuxMain) }
        getByName("macosArm64Main") { dependsOn(macosMain) }
        getByName("mingwX64Main") { dependsOn(windowsMain) }

        val macosTest by creating {
            dependsOn(nativeTest)
            kotlin.srcDirs("tests/platform/macos/kotlin")
        }

        getByName("linuxX64Test") { dependsOn(nativeTest) }
        getByName("macosArm64Test") { dependsOn(macosTest) }
        getByName("mingwX64Test") { dependsOn(nativeTest) }

        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
            binaries.all {
                if (this is org.jetbrains.kotlin.gradle.plugin.mpp.Executable) {
                    val runTaskName = "run${name.replaceFirstChar { it.uppercase() }}${targetName.replaceFirstChar { it.uppercase() }}"
                    tasks.matching { it.name == runTaskName }.configureEach {
                        val runTask = this as? Exec
                        runTask?.let {
                            val argsProperty = project.findProperty("args") as? String
                            if (argsProperty != null) {
                                it.args(*argsProperty.split(" ").toTypedArray())
                            }
                        }
                    }
                }
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest>().configureEach {
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        events("passed", "failed", "skipped")
        showExceptions = true
        showCauses = true
        showStackTraces = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.register<Exec>("dmg") {
    group = "package"
    description = "Packages the macOS application as a DMG (macOS only)"
    dependsOn("icon")

    val target = if (System.getProperty("os.arch") == "aarch64") "macosArm64" else "macosX64"
    dependsOn("linkReleaseExecutable${target.replaceFirstChar { it.uppercase() }}")

    commandLine("make", "dmg")
}

tasks.register<Exec>("icon") {
    group = "build"
    description = "Generates application icons"
    commandLine("make", "icon")
}

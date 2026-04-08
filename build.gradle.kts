import org.gradle.api.tasks.Exec

plugins {
    kotlin("multiplatform") version "2.1.10"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
}

ktlint {
    version.set("1.5.0")
    verbose.set(true)
    outputToConsole.set(true)
    enableExperimentalRules.set(true)
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/stable")
}

kotlin {
    // Native targets
    linuxX64 {
        binaries.executable {
            baseName = "Ofis"
            entryPoint = "ofis.main"
        }
    }
    macosX64 {
        binaries.executable {
            baseName = "Ofis"
            entryPoint = "ofis.main"
        }
    }
    macosArm64 {
        binaries.executable {
            baseName = "Ofis"
            entryPoint = "ofis.main"
        }
    }
    mingwX64 {
        binaries.executable {
            baseName = "Ofis"
            entryPoint = "ofis.main"
        }
    }

    // Web target
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlinx.cinterop.BetaInteropApi")
        }
        val commonMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.1")
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.components.resources)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.uiToolingPreview)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }

        val macosMain by creating {
            dependsOn(nativeMain)
        }
        val linuxMain by creating {
            dependsOn(nativeMain)
        }
        val mingwMain by creating {
            dependsOn(nativeMain)
        }

        val linuxX64Main by getting { dependsOn(linuxMain) }
        val macosX64Main by getting { dependsOn(macosMain) }
        val macosArm64Main by getting { dependsOn(macosMain) }
        val mingwX64Main by getting { dependsOn(mingwMain) }

        val linuxX64Test by getting { dependsOn(nativeTest) }
        val macosX64Test by getting { dependsOn(nativeTest) }
        val macosArm64Test by getting { dependsOn(nativeTest) }
        val mingwX64Test by getting { dependsOn(nativeTest) }

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

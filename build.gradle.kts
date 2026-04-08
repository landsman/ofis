plugins {
    kotlin("multiplatform") version "2.1.10"
}

repositories {
    mavenCentral()
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

        listOf(
            linuxX64(),
            macosX64(),
            macosArm64(),
            mingwX64()
        ).forEach { target ->
            val mainSourceSet = target.compilations.getByName("main").defaultSourceSet
            mainSourceSet.dependsOn(nativeMain)
            target.compilations.getByName("test").defaultSourceSet.dependsOn(nativeTest)

            if (target.name.startsWith("macos")) {
                mainSourceSet.dependsOn(macosMain)
            } else if (target.name.startsWith("linux")) {
                mainSourceSet.dependsOn(linuxMain)
            } else if (target.name.startsWith("mingw")) {
                mainSourceSet.dependsOn(mingwMain)
            }
            
            target.binaries.all {
                if (this is org.jetbrains.kotlin.gradle.plugin.mpp.Executable) {
                    val runTaskName = "run${name.replaceFirstChar { it.uppercase() }}${target.name.replaceFirstChar { it.uppercase() }}"
                    tasks.matching { it.name == runTaskName }.configureEach {
                        val runTask = this as? org.gradle.api.tasks.Exec
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

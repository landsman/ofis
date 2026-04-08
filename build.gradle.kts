import org.gradle.api.tasks.Exec
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("multiplatform") version "2.1.10"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("org.owasp.dependencycheck") version "10.0.4"
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
}

tasks.withType(DependencyUpdatesTask::class.java).configureEach {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
    checkForGradleUpdate = true
    outputFormatter = "json,plain"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}

dependencyCheck {
    failBuildOnCVSS = 7.0.toFloat()
    formats = listOf("HTML", "JSON")
    analyzers {
        assemblyEnabled = false
    }

    // Load NVD API key from .env file or environment variable
    val envFile = file(".env")
    var nvdKey: String? = System.getenv("NVD_API_KEY")

    if (nvdKey == null && envFile.exists()) {
        envFile.useLines { lines ->
            lines.forEach { line ->
                if (line.trim().startsWith("NVD_API_KEY=")) {
                    nvdKey = line.substringAfter("=").trim().trim('"').trim('\'')
                }
            }
        }
    }

    if (nvdKey != null) {
        nvd {
            apiKey = nvdKey
        }
    }
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


// Configure Detekt reports for all Detekt tasks
tasks.withType(Detekt::class.java).configureEach {
    reports {
        html.required.set(true)
        sarif.required.set(true)
        txt.required.set(true)
        xml.required.set(false)
        md.required.set(false)
    }
}

// Aggregate task to run Detekt on all relevant source sets to detect unused code/imports
// Usage: ./gradlew analyzeUnused
// Reports will be written under build/reports/detekt
tasks.register("analyzeUnused") {
    group = "verification"
    description = "Runs Detekt across KMP source sets to report unused imports/members and other issues"
    dependsOn(
        // NOTE: Skip detektMetadataCommonMain due to a stale path in a third-party configuration
        // The following tasks still analyze common code transitively
        "detektMetadataNativeMain",
        "detektMetadataMacosMain",
        "detektLinuxX64Main",
        "detektMacosX64Main",
        "detektMacosArm64Main",
        "detektMingwX64Main",
        "detektWasmJsMain"
    )
}

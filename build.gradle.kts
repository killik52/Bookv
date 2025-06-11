plugins {
    id("com.android.application") version "8.9.2" apply false
    // Updated Kotlin plugin version to match libs.versions.toml
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    // Updated KSP plugin version for compatibility with Kotlin 2.0.20
    id("com.google.devtools.ksp") version "2.0.20-1.0.24" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // As dependências aqui geralmente são para plugins do próprio Android/Kotlin
        classpath("com.android.tools.build:gradle:8.9.2") // Verifique a versão mais recente
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23") // Verifique a versão mais recente
    }
}

plugins {
    id("com.android.application") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.19" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.9.2" apply false
    // CORREÇÃO: Versão do Kotlin atualizada para uma versão estável mais recente
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    // CORREÇÃO: Versão do KSP atualizada para ser compatível com o Kotlin 2.0.0
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
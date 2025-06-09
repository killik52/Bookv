// Este bloco informa ao Gradle onde procurar os plugins que você vai usar
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // Adiciona o portal de plugins do Gradle
    }
}

// Este bloco informa onde procurar as dependências (bibliotecas) do seu app
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BookV6"
include(":app")
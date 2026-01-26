pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
//        maven { url = uri("https://pay.cards/maven") }
        maven(url = "https://jitpack.io")
        jcenter() // ✅ مؤقتًا عشان blurry

    }
}

rootProject.name = "EthioPay"
//include(":app")
include(":app", ":library")

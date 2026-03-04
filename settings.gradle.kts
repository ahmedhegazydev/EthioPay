import org.gradle.authentication.http.BasicAuthentication

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

        // ✅ JitPack (Private) with token from gradle.properties -> authToken=jp_...
        maven(url = "https://jitpack.io") {
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                // Don't crash sync if missing, but you'll still get 401 later (which is correct)
                username = providers.gradleProperty("authToken").orNull
                password = ""
            }
        }
        // Scanbot repos
        maven(url = "https://nexus.scanbot.io/nexus/content/repositories/releases/")
        maven(url = "https://nexus.scanbot.io/nexus/content/repositories/snapshots/")
    }
}

rootProject.name = "EthioPay"
include(
    ":app",
    ":libidsdk",
    ":libfotoapparat"
)

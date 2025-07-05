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

        // TEMP: Jan Tennert GitHub Packages if needed
        maven { url = uri("https://maven.pkg.github.com/jaumard/supabase-kt") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        
        // Mapbox Maven repository with credentials
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                // These should be the same as in your gradle.properties file
                username = "mapbox"
                // Use either the environment variable or the gradle property
                password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").orNull 
                    ?: System.getenv("MAPBOX_DOWNLOADS_TOKEN") 
                    ?: "" // Fallback to empty string if not set (will fail with 401)
            }
        }
    }
}

rootProject.name = "Athlo"
include(":app")
 
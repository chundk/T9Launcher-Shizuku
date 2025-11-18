@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    
    versionCatalogs {
        create("libs") {
            version("kotlin", "2.2.20")
            version("androidGradlePlugin", "8.13.0")
            version("hilt", "2.57.2")
            
            plugin("android.application", "com.android.application").versionRef("androidGradlePlugin")
            plugin("kotlin.android", "org.jetbrains.kotlin.android").versionRef("kotlin")
            plugin("kotlin.compose", "org.jetbrains.kotlin.plugin.compose").versionRef("kotlin")
            plugin("kotlin.kapt", "org.jetbrains.kotlin.kapt").versionRef("kotlin")
            plugin("hilt.android", "com.google.dagger.hilt.android").versionRef("hilt")
        }
    }
}

// 包含shizuku-hidden-api-stub模块
include(":shizuku-hidden-api-stub")

rootProject.name = "T9 Launcher"
include(":app")

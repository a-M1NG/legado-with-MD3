pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        maven { url 'https://maven.aliyun.com/repository/public' }
    }
}
plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven {
            url = uri("https://jitpack.io")
            content {
                includeGroupByRegex("com\\.github.*")
            }
        }
        mavenCentral()
    }
}
rootProject.name = 'legado-kmp'

// KMP Core Modules
include ':core:common'
include ':core:data'
include ':core:network'
include ':core:storage'
include ':core:js-engine'

// Platform Implementations
include ':platforms:android'
include ':platforms:ios'
include ':platforms:desktop'
include ':platforms:web'

// Legacy modules (to be migrated gradually)
include ':modules:book'
include ':modules:rhino'
include ':modules:web'
include ':app'
include ':baselineprofile'
project(':baselineprofile').projectDir = file('baselineProfile')

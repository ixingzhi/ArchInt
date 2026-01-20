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

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 官方源
        google()
        mavenCentral()
        // 第三方与镜像
        maven(url = uri("https://jitpack.io"))
        maven(url = uri("https://maven.aliyun.com/repository/google/"))
        maven(url = uri("https://maven.aliyun.com/repository/public/"))
        maven(url = uri("https://repo1.maven.org/maven2/"))
    }
}

rootProject.name = "ArchInt"
include(":app")
include(":module-base")
include(":module-biz-account")
include(":module-biz-account-api")
include(":module-biz-home")
include(":module-biz-mine")
include(":module-biz-mine-api")

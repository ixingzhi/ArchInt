import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.ixingzhi.archint.base"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    viewBinding {
        enable = true
    }

    // 启用 buildConfig（插件需要）
    buildFeatures {
        buildConfig = true
    }

    // resourcePrefix = ""
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}

/**
 *  Flavor-specific 依赖配置
 *  Android Gradle Plugin 在配置了 product flavors 后会自动创建这些配置
 *  在 Kotlin DSL 中，需要使用字符串形式来访问这些配置
 *
 *  使用方式示例：
 *  1. 直接使用字符串形式（推荐）：
 *     add("cnImplementation", "com.example:library:1.0.0")
 *     add("globalImplementation", "com.example:library:1.0.0")
 *  2. 使用 libs 版本目录：
 *     add("cnImplementation", libs.some.cn.specific.library)
 *     add("globalImplementation", libs.some.global.specific.library)
 *  3. 使用 Api 配置：
 *     add("cnApi", libs.some.cn.specific.library)
 *     add("globalApi", libs.some.global.specific.library)
 */
dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.core)
    api(libs.androidx.appcompat)
    api(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.activity.compose)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.lifecycle.runtime.ktx)

    api(libs.alibaba.arouter.api)
    kapt(libs.alibaba.arouter.compiler)
}
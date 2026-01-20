import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = libs.versions.applicationId.get()
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = libs.versions.applicationId.get()
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // abiFilters 'armeabi-v7a', "arm64-v8a", 'x86', 'x86_64'
            // armeabi-v7a 2011-2018 年的主流 Android 手机，低端或入门级设备。
            abiFilters.add("arm64-v8a")
        }
    }

    applicationVariants.all {
        val variant = this
        val project = libs.versions.projectName.get()
        val buildType = variant.buildType.name
        val flavorName = variant.flavorName
        val versionName = variant.versionName
        val date = SimpleDateFormat("yyyyMMddHHmm").format(Date())

        variant.outputs.all {
            when (this) {
                is com.android.build.gradle.internal.api.ApkVariantOutputImpl -> {
                    outputFileName =
                        "${project}_${flavorName}_${buildType}_v${versionName}_${date}.apk"
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    buildFeatures {
        compose = true
    }

    // 启用 buildConfig（插件需要）
    buildFeatures {
        buildConfig = true
    }
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}

configurations.all {
    exclude(group = "com.android.support")
}

dependencies {
    kapt(libs.alibaba.arouter.compiler)
    implementation(project(":module-base"))
    implementation(project(":module-biz-home"))
    implementation(project(":module-biz-mine"))
    implementation(project(":module-biz-account"))
}
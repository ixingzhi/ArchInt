package com.ixingzhi.archint.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Product Flavor 配置插件
 *
 * 自动从根项目的 extra 中读取配置并应用到 Android 模块
 *
 * 使用方法：
 * ```kotlin
 * plugins {
 *     id("com.ixingzhi.archint.gradle.flavor-config")
 * }
 * ```
 */
class FlavorConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // 在插件应用时注册配置，需要在配置阶段执行，不能太晚，使用 whenPluginApplied 确保在 Android 插件应用后执行。
        project.plugins.withId("com.android.application") {
            // 直接配置，在配置阶段执行
            val androidExtension = project.extensions.findByName("android")
            if (androidExtension != null) {
                applyFlavorConfig(project, androidExtension)
            } else {
                // 如果 android 扩展还未创建，延迟到 afterEvaluate
                project.afterEvaluate {
                    val android = project.extensions.findByName("android")
                    if (android != null) {
                        applyFlavorConfig(project, android)
                    }
                }
            }
        }
        project.plugins.withId("com.android.library") {
            val androidExtension = project.extensions.findByName("android")
            if (androidExtension != null) {
                applyFlavorConfig(project, androidExtension)
            } else {
                project.afterEvaluate {
                    val android = project.extensions.findByName("android")
                    if (android != null) {
                        applyFlavorConfig(project, android)
                    }
                }
            }
        }
    }

    private fun applyFlavorConfig(project: Project, android: Any) {
        try {
            val rootProject = project.rootProject

            // 启用 buildConfig（如果模块中已经启用，这里会失败，但可以忽略），注意：buildConfig 可能已经在模块的 build.gradle.kts 中启用
            try {
                val buildFeaturesMethod = android.javaClass.getMethod("getBuildFeatures")
                val buildFeatures = buildFeaturesMethod.invoke(android)
                try {
                    val buildConfigField = buildFeatures.javaClass.getDeclaredField("buildConfig")
                    buildConfigField.isAccessible = true
                    buildConfigField.setBoolean(buildFeatures, true)
                } catch (_: NoSuchFieldException) {
                    // 如果字段不存在，尝试使用 setter 方法
                    try {
                        val setBuildConfigMethod =
                            buildFeatures.javaClass.getMethod("setBuildConfig", Boolean::class.java)
                        setBuildConfigMethod.invoke(buildFeatures, true)
                    } catch (_: Exception) {
                        // 如果都失败，buildConfig 可能已经启用
                    }
                }
            } catch (_: Exception) {
                // buildConfig 可能已经启用，或者无法通过反射设置，忽略错误
            }

            // 应用 Flavor Dimensions，需要在创建 productFlavor 之前设置
            try {
                val flavorDimensionsList =
                    rootProject.extensions.extraProperties.get("flavorDimensions") as? List<*>
                flavorDimensionsList?.forEach { dimension ->
                    try {
                        val flavorDimensionsMethod =
                            android.javaClass.getMethod("getFlavorDimensions")

                        @Suppress("UNCHECKED_CAST")
                        val flavorDimensions =
                            flavorDimensionsMethod.invoke(android) as? MutableList<String>
                        val dimStr = dimension.toString()
                        if (flavorDimensions != null && !flavorDimensions.contains(dimStr)) {
                            flavorDimensions.add(dimStr)
                        }
                    } catch (_: Exception) {
                        // 如果无法设置，忽略
                    }
                }
            } catch (_: Exception) {
                // 忽略错误
            }

            // 应用 Market Flavors 配置，需要在配置阶段执行，不能太晚
            val marketFlavors =
                rootProject.extensions.extraProperties.get("marketFlavors") as? Map<*, *>
            marketFlavors?.forEach { (flavorName, flavorConfig) ->
                val config = flavorConfig as? Map<*, *>
                config?.let {
                    val name = flavorName.toString()

                    try {
                        // 获取 productFlavors 并创建 flavor
                        val productFlavorsMethod = android.javaClass.getMethod("getProductFlavors")
                        val productFlavors = productFlavorsMethod.invoke(android)
                        val createMethod =
                            productFlavors.javaClass.getMethod("create", String::class.java)
                        val flavor = createMethod.invoke(productFlavors, name)

                        // 设置 dimension
                        try {
                            val dimensionField = flavor.javaClass.getDeclaredField("dimension")
                            dimensionField.isAccessible = true
                            dimensionField.set(flavor, "market")
                        } catch (_: Exception) {
                            // 如果字段访问失败，尝试使用 setter 方法
                            try {
                                val setDimensionMethod =
                                    flavor.javaClass.getMethod("setDimension", String::class.java)
                                setDimensionMethod.invoke(flavor, "market")
                            } catch (e2: Exception) {
                                project.logger.warn("Failed to set dimension for flavor '$name': ${e2.message}")
                            }
                        }

                        // 应用 applicationIdSuffix（仅对 Application 模块有效）
                        (it["applicationIdSuffix"] as? String)?.let { suffix ->
                            try {
                                // 检查是否是 AppExtension
                                val isAppExtension = android.javaClass.name.contains("AppExtension")
                                if (isAppExtension) {
                                    val method = flavor.javaClass.getMethod(
                                        "setApplicationIdSuffix",
                                        String::class.java
                                    )
                                    method.invoke(flavor, suffix)
                                }
                            } catch (_: Exception) {
                                // Library 模块不支持，忽略
                            }
                        }

                        // 应用 versionNameSuffix（仅对 Application 模块有效）
                        (it["versionNameSuffix"] as? String)?.let { suffix ->
                            try {
                                // 检查是否是 AppExtension
                                val isAppExtension = android.javaClass.name.contains("AppExtension")
                                if (isAppExtension) {
                                    val method = flavor.javaClass.getMethod(
                                        "setVersionNameSuffix",
                                        String::class.java
                                    )
                                    method.invoke(flavor, suffix)
                                }
                            } catch (_: Exception) {
                                // Library 模块不支持，忽略
                            }
                        }

                        // 应用 resValue
                        (it["resValue"] as? Map<*, *>)?.let { resValue ->
                            val type = resValue["type"] as? String ?: "string"
                            val resName = resValue["name"] as? String
                            val resValueStr = resValue["value"] as? String
                            if (resName != null && resValueStr != null) {
                                try {
                                    val resValueMethod = flavor.javaClass.getMethod(
                                        "resValue",
                                        String::class.java,
                                        String::class.java,
                                        String::class.java
                                    )
                                    resValueMethod.invoke(flavor, type, resName, resValueStr)
                                } catch (e: Exception) {
                                    project.logger.warn("Failed to set resValue: ${e.message}")
                                }
                            }
                        }

                        // 应用 buildConfigFields
                        (it["buildConfigFields"] as? List<*>)?.forEach { field ->
                            val fieldMap = field as? Map<*, *>
                            fieldMap?.let { map ->
                                val type = map["type"] as? String
                                val fieldName = map["name"] as? String
                                val fieldValue = map["value"] as? String
                                if (type != null && fieldName != null && fieldValue != null) {
                                    try {
                                        val buildConfigFieldMethod = flavor.javaClass.getMethod(
                                            "buildConfigField",
                                            String::class.java,
                                            String::class.java,
                                            String::class.java
                                        )
                                        buildConfigFieldMethod.invoke(
                                            flavor, type, fieldName, fieldValue
                                        )
                                    } catch (e: Exception) {
                                        project.logger.warn("Failed to set buildConfigField: ${e.message}")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        project.logger.warn("Failed to create flavor '$name': ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            project.logger.error("Error applying flavor config: ${e.message}", e)
            throw e
        }
    }

}
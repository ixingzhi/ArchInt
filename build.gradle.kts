import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

/**
 * Top-level build file where you can add configuration options common to all sub-projects/modules.
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
}

/**
 * 全局配置：为所有 Android Library 和 Application 模块自动添加通用插件和配置
 */
subprojects {
    // 在beforeEvaluate中加载，每个模块中可以正常加载flavor配置。
    beforeEvaluate {
        val androidPlugins = listOf("com.android.library", "com.android.application")
        val commonPlugins = listOf(
            "org.jetbrains.kotlin.plugin.compose",
            "kotlin-parcelize",
            "com.ixingzhi.archint.gradle.flavor-config" // 每一个模块配置flavor
        )

        androidPlugins.forEach { androidPlugin ->
            pluginManager.withPlugin(androidPlugin) {
                // 自动应用通用 Kotlin 插件（apply 方法是幂等的，已应用不会重复）
                commonPlugins.forEach { plugin ->
                    pluginManager.apply(plugin)
                }
            }
        }
    }

    afterEvaluate {
    }
}

val localProperties = Properties().apply {
    load(rootProject.file("local.properties").reader())
}

/**
 * 创建 BuildConfig 字段配置
 */
fun buildConfigField(type: String, name: String, value: String) = mapOf(
    "type" to type, "name" to name, "value" to value
)

/**
 * 创建 ResValue 配置
 */
fun resValue(type: String, name: String, value: String) = mapOf(
    "type" to type, "name" to name, "value" to value
)

/**
 * 获取 Git Commit ID（短版本）
 */
fun getGitCommitId(): String {
    return try {
        @Suppress("DEPRECATION") val process =
            Runtime.getRuntime().exec("git rev-parse --short HEAD")
        process.inputStream.bufferedReader().use { it.readLine()?.trim() ?: "unknown" }
    } catch (_: Exception) {
        "unknown"
    }
}

/**
 * 获取编译时间
 */
fun getBuildTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
}

/**
 * 创建公共的 BuildConfig 字段列表（所有 flavor 共享）
 */
fun createCommonBuildConfigFields(): List<Map<String, String>> {
    return listOf(
        buildConfigField("String", "BUILD_TIME", "\"${getBuildTime()}\""),
        buildConfigField("String", "GIT_COMMIT_ID", "\"${getGitCommitId()}\""),
    )
}

/**
 * 创建市场特定的 BuildConfig 字段列表
 */
fun createMarketSpecificBuildConfigFields(flavorName: String): List<Map<String, String>> {
    val baseFields = listOf(
        buildConfigField("String", "FLAVOR_NAME", "\"$flavorName\""),
        buildConfigField("String", "API_BASE_URL", "\"https://app.xxx.com/\""),
        buildConfigField("String", "WEB_BASE_URL", "\"https://h5.xxx.com/\"")
    )

    // CN 市场特有的配置
    if (flavorName == "cn") {
        return baseFields + listOf(
            buildConfigField(
                "String",
                "CUSTOM_XXX_XXX",
                "\"${localProperties.getProperty("CUSTOM_XXX_XXX", "")}\""
            ),
        )
    } else if (flavorName == "global") {
        return baseFields + listOf(
            buildConfigField(
                "String",
                "CUSTOM_XXX_XXX",
                "\"${localProperties.getProperty("CUSTOM_XXX_XXX", "")}\""
            ),
        )
    }

    return baseFields
}

/**
 * 创建市场 Cn Flavor 配置
 */
fun createCnMarketFlavor(): Map<String, Any> = buildMap {
    put("applicationIdSuffix", ".cn")
    put("versionNameSuffix", "-cn")
    put("resValue", resValue("string", "app_name", "ArchInt(CN)"))
    put(
        "buildConfigFields",
        createCommonBuildConfigFields() + createMarketSpecificBuildConfigFields("cn")
    )
}

/**
 * 创建市场 Global Flavor 配置
 */
fun createGlobalMarketFlavor(): Map<String, Any> = buildMap {
    put("applicationIdSuffix", ".global")
    put("versionNameSuffix", "-global")
    put("resValue", resValue("string", "app_name", "ArchInt(GLOBAL)"))
    put(
        "buildConfigFields",
        createCommonBuildConfigFields() + createMarketSpecificBuildConfigFields("global")
    )
}

extra.apply {
    // Flavor 维度配置，注意：如果将来需要添加 environment 维度（如 dev、staging、production），可以在这里添加
    set("flavorDimensions", listOf("market"))

    /*
       市场维度配置，注意：buildConfigFields 使用列表存储，避免 Map 中相同键被覆盖的问题
       公共的 buildConfigFields（BUILD_TIME、GIT_COMMIT_ID、HTTP_TEST_DEFAULT）已通过 createCommonBuildConfigFields() 自动包含
     */
    set(
        "marketFlavors", buildMap {
            // CN 市场配置
            put("cn", createCnMarketFlavor())
            // Global 市场配置
            put("global", createGlobalMarketFlavor())
        }
    )
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
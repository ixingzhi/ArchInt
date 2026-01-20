plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // 不需要直接依赖 AGP，使用反射访问
}

// 注册插件
gradlePlugin {
    plugins {
        create("flavorConfig") {
            id = "com.ixingzhi.archint.gradle.flavor-config"
            implementationClass = "com.ixingzhi.archint.gradle.FlavorConfigPlugin"
            description = "自动应用 Product Flavor 配置的插件"
        }
    }
}
# ArchInt（ARCHITECTURE_INTERNATIONAL） 项目国际化+组件化+Compose（CN/Global）实现总结

## 📋 项目概述

ArchInt 项目实现了完整的多渠道架构，支持**国内版（CN）**和**国际版（Global）**两个渠道，能够：

- 同一页面实现差异项（如设置页面：国版有广告、无语言选项；国际版无广告、有语言选项）
- 不同渠道实现完全不同的页面（如登录页面：国版使用手机号登录，国际版使用邮箱登录）

---

## 🏗️ 项目结构

### 1. 模块架构

```
ArchInt/
├── app/                          # 主应用模块
├── module-base/                  # 基础模块（公共代码、主题、路由等）
├── module-biz-account/           # 账户业务模块（实现）
│   ├── src/main/                # 公共代码
│   ├── src/cn/                  # 国版特定实现
│   └── src/global/              # 国际版特定实现
├── module-biz-account-api/       # 账户业务API模块（接口定义）
├── module-biz-home/             # 首页业务模块
│   ├── src/main/                # 公共代码
│   ├── src/cn/                  # 国版特定资源
│   └── src/global/              # 国际版特定资源
├── module-biz-mine/             # 我的页面业务模块
│   ├── src/main/                # 公共代码
│   ├── src/cn/                  # 国版特定实现
│   └── src/global/              # 国际版特定实现
├── module-biz-mine-api/         # 我的页面API模块
└── buildSrc/                    # 自定义 Gradle 插件
    └── FlavorConfigPlugin.kt    # Flavor 配置插件
```

### 2. 目录结构说明

#### 标准目录结构

- `src/main/` - 所有渠道共享的代码和资源
- `src/cn/` - 国版（CN）特定的代码和资源
- `src/global/` - 国际版（Global）特定的代码和资源

#### Android 构建系统自动选择

- 编译 `cnDebug` 时：使用 `main` + `cn` 目录
- 编译 `globalDebug` 时：使用 `main` + `global` 目录

---

## 🔧 实现方式

### 一、Gradle 配置层

#### 1. 根项目配置 (`build.gradle.kts`)

**Flavor 定义：**

```kotlin
extra.apply {
    set("flavorDimensions", listOf("market"))
    set("marketFlavors", buildMap {
        put("cn", createCnMarketFlavor())      // 国版配置
        put("global", createGlobalMarketFlavor()) // 国际版配置
    })
}
```

**Flavor 配置内容：**

- `applicationIdSuffix`: 包名后缀（`.cn` / `.global`）
- `versionNameSuffix`: 版本名后缀（`-cn` / `-global`）
- `resValue`: 资源值（如应用名称）
- `buildConfigFields`: BuildConfig 字段（FLAVOR_NAME、API_BASE_URL 等）

#### 2. 自定义 Gradle 插件 (`FlavorConfigPlugin`)

**功能：**

- 自动为所有 Android 模块应用 flavor 配置
- 通过反射动态设置 `productFlavors`
- 支持 `applicationIdSuffix`、`versionNameSuffix`、`resValue`、`buildConfigFields`

**应用方式：**

```kotlin
// build.gradle.kts (根项目)
subprojects {
    beforeEvaluate {
        val commonPlugins = listOf(
            "com.ixingzhi.archint.gradle.flavor-config" // 自动应用
        )
    }
}
```

---

### 二、代码实现层

#### 方式一：同一页面差异项实现（推荐）

**适用场景：** 页面结构相同，但部分内容不同

**实现模式：接口 + 差异实现**

**示例：账户设置页面 (`SettingsActivity`)**

1. **定义接口** (`SettingsDiffListener.kt`)

```kotlin
interface SettingsDiffListener {
    fun getItems(): Map<String, ImageVector>

    @Composable
    fun getBottomADView()
}
```

2. **国版实现** (`src/cn/.../SettingsDiffImpl.kt`)

```kotlin
object SettingsDiffImpl : SettingsDiffListener {
    override fun getItems(): Map<String, ImageVector> {
        // 国版：Account, Theme, Logout（无 Language）
        return mapOf(
            SettingsItemKeys.ACCOUNT to Icons.Filled.AccountCircle,
            SettingsItemKeys.THEME to Icons.Filled.DarkMode,
            SettingsItemKeys.LOGOUT to Icons.Filled.ExitToApp
        )
    }

    @Composable
    override fun getBottomADView() {
        // 国版：显示广告区域
        Card(...) { Text("底部固定AD区域") }
    }
}
```

3. **国际版实现** (`src/global/.../SettingsDiffImpl.kt`)

```kotlin
object SettingsDiffImpl : SettingsDiffListener {
    override fun getItems(): Map<String, ImageVector> {
        // 国际版：Account, Theme, Language, Logout（有 Language）
        return mapOf(
            SettingsItemKeys.ACCOUNT to Icons.Filled.AccountCircle,
            SettingsItemKeys.THEME to Icons.Filled.DarkMode,
            SettingsItemKeys.LANGUAGE to Icons.Filled.Language, // 差异项
            SettingsItemKeys.LOGOUT to Icons.Filled.ExitToApp
        )
    }

    @Composable
    override fun getBottomADView() {
        // 国际版：不显示广告（空实现）
    }
}
```

4. **公共页面使用**

```kotlin
@Composable
fun ListWithBottomAdViewScreen() {
    val itemList = SettingsDiffImpl.getItems() // 自动选择对应实现
    // ...
    SettingsDiffImpl.getBottomADView() // 自动选择对应实现
}
```

**优势：**

- ✅ 代码复用率高
- ✅ 差异点清晰明确
- ✅ 易于维护和扩展

---

#### 方式二：不同渠道完全不同的页面

**适用场景：** 页面结构完全不同

**实现模式：同名类在不同 flavor 目录**

**示例：登录页面 (`LoginActivity`)**

1. **国版实现** (`src/cn/.../LoginActivity.kt`)

```kotlin
class LoginActivity : ComponentActivity() {
    // 国版：手机号登录
    @Composable
    fun LoginScreen() {
        OutlinedTextField(
            label = { Text("手机号") },
            leadingIcon = { Icon(Icons.Filled.Phone) },
            keyboardType = KeyboardType.Phone
        )
    }
}
```

2. **国际版实现** (`src/global/.../LoginActivity.kt`)

```kotlin
class LoginActivity : ComponentActivity() {
    // 国际版：邮箱登录
    @Composable
    fun LoginScreen() {
        OutlinedTextField(
            label = { Text("邮箱") },
            leadingIcon = { Icon(Icons.Filled.Email) },
            keyboardType = KeyboardType.Email
        )
    }
}
```

3. **公共路由** (`src/main/.../LoginLauncherImpl.kt`)

```kotlin
@Route(path = LoginLauncher.LAUNCHER)
class LoginLauncherImpl : LoginLauncher {
    override fun startActivity(context: Context) {
        context.startActivity(LoginActivity.createIntent(context))
        // 自动选择对应 flavor 的 LoginActivity
    }
}
```

**优势：**

- ✅ 完全独立的实现
- ✅ 互不干扰
- ✅ 适合差异较大的页面

---

### 三、资源文件差异

#### 资源目录结构

```
module-biz-account/src/
├── main/res/          # 公共资源
├── cn/res/            # 国版特定资源
└── global/res/        # 国际版特定资源
```

#### 使用场景

- 字符串资源（`strings.xml`）
- 图片资源（`drawable/`）
- 布局文件（`layout/`）
- 颜色、样式等

---

## 📦 BuildConfig 配置

### 自动生成的字段

每个模块的 BuildConfig 自动包含：

**公共字段：**

- `BUILD_TIME`: 编译时间
- `GIT_COMMIT_ID`: Git 提交 ID

**Flavor 特定字段：**

- `FLAVOR_NAME`: "cn" 或 "global"
- `API_BASE_URL`: API 基础地址
- `WEB_BASE_URL`: Web 基础地址
- `CUSTOM_XXX_XXX`: 自定义配置（从 `local.properties` 读取）

### 使用示例

```kotlin
// HomeScreen.kt
val flavorName = BuildConfig.FLAVOR // "cn" 或 "global"
val versionText = when (flavorName.lowercase()) {
    "cn" -> "国内版"
    "global" -> "国际版"
    else -> "未知版本"
}
```

---

## 🔄 路由系统（ARouter）

### 架构设计

**API 模块（接口定义）：**

- `module-biz-account-api`: 定义 `LoginLauncher` 接口
- `module-biz-mine-api`: 定义 `SettingsLauncher` 接口

**实现模块（具体实现）：**

- `module-biz-account`: 实现 `LoginLauncherImpl`
- `module-biz-mine`: 实现 `SettingsLauncherImpl`

### 路由使用

```kotlin
// 统一的路由调用
Launcher.navigation(LoginLauncher::class.java)?.startActivity(context)
```

**优势：**

- ✅ 模块间解耦
- ✅ 支持不同实现
- ✅ 类型安全

---

## 🎯 实现过程总结

### 第一步：配置 Gradle Flavor

1. **根项目配置** (`build.gradle.kts`)
    - 定义 flavor 维度：`market`
    - 配置 CN 和 Global 的 flavor 参数

2. **自定义插件** (`FlavorConfigPlugin`)
    - 自动为所有模块应用 flavor 配置
    - 通过反射设置 `productFlavors`

### 第二步：创建目录结构

```
module-biz-account/src/
├── main/        # 公共代码
├── cn/          # 国版特定代码
└── global/      # 国际版特定代码
```

### 第三步：实现差异逻辑

**方式一：接口模式（推荐用于差异项）**

1. 在 `main` 中定义接口
2. 在 `cn` 和 `global` 中分别实现
3. 公共代码调用接口

**方式二：同名类模式（用于完全不同的页面）**

1. 在 `cn` 和 `global` 中分别实现同名类
2. 构建系统自动选择对应实现

### 第四步：配置依赖

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":module-biz-account"))      // 实现模块
    implementation(project(":module-biz-account-api"))   // API 模块（可选）
}
```

---

## ✨ 核心优势

1. **代码复用**：公共代码在 `main` 目录，避免重复
2. **清晰分离**：差异代码在 flavor 目录，易于维护
3. **自动选择**：构建系统自动选择对应实现，无需手动判断
4. **类型安全**：通过接口和 BuildConfig 保证类型安全
5. **易于扩展**：新增 flavor 只需添加新目录和配置

---

## 📝 最佳实践

### 1. 差异项实现

- ✅ 使用接口模式（`DiffListener`）
- ✅ 在 `main` 中定义接口和常量
- ✅ 在 flavor 目录中实现差异逻辑

### 2. 完全不同的页面

- ✅ 使用同名类模式
- ✅ 保持相同的包名和类名
- ✅ 通过路由统一调用

### 3. 资源文件

- ✅ 公共资源放在 `main/res`
- ✅ 差异资源放在 flavor 目录
- ✅ 使用 `resourcePrefix` 避免冲突

### 4. BuildConfig

- ✅ 通过 `FLAVOR_NAME` 判断当前渠道
- ✅ 使用 `FLAVOR` 字段（自动生成）
- ✅ 自定义字段通过插件配置

---

## 🔍 实际案例

### 案例一：账户设置页面（差异项实现）

**差异点：**

- 国版：无语言选项，有广告
- 国际版：有语言选项，无广告

**实现：**

- 接口：`SettingsDiffListener`
- 国版实现：`src/cn/.../SettingsDiffImpl`
- 国际版实现：`src/global/.../SettingsDiffImpl`
- 公共页面：`SettingsActivity` 调用接口

### 案例二：登录页面（完全不同的页面）

**差异点：**

- 国版：手机号登录
- 国际版：邮箱登录

**实现：**

- 国版：`src/cn/.../LoginActivity.kt`
- 国际版：`src/global/.../LoginActivity.kt`
- 路由：`LoginLauncherImpl` 统一调用

---

## 🚀 构建和运行

### 构建命令

```bash
# 构建国版 Debug
./gradlew assembleCnDebug

# 构建国际版 Debug
./gradlew assembleGlobalDebug

# 安装国版
./gradlew installCnDebug

# 安装国际版
./gradlew installGlobalDebug
```

### 输出文件

```
app/build/outputs/apk/
├── cn/debug/
│   └── ArchInt_cn_debug_v1.0.0_202601201200.apk
└── global/debug/
    └── ArchInt_global_debug_v1.0.0_202601201200.apk
```

### 小工具

**快速创建CN/Global文件夹**

```
// 模块名替换成项目实际名称
mkdir -p module-biz-mine/src/cn/java/com/ixingzhi/archint/mine;
mkdir -p module-biz-mine/src/cn/res/{layout,values,drawable};      
mkdir -p module-biz-mine/src/global/java/com/ixingzhi/archint/mine;
mkdir -p module-biz-mine/src/global/res/{layout,values,drawable}
```

**快速填充空占位目录（git空文件夹不会上传）**

```
// 在项目根目录执行（确保你在正确的目录）
find . -type d -empty -not -path "./.git/*" -exec touch {}/.keep \;
```

---

## 📚 总结

ArchInt 项目通过 **Gradle Flavor + 目录结构 + 接口模式** 实现了完整的多渠道架构：

1. **配置层**：通过自定义 Gradle 插件自动配置所有模块的 flavor
2. **代码层**：通过 `main/cn/global` 目录结构实现代码差异
3. **接口层**：通过接口模式实现差异项的统一管理
4. **路由层**：通过 ARouter 实现模块间解耦

这种架构既保证了代码复用，又实现了清晰的差异管理，是 Android 国际化多渠道开发的最佳实践。
package com.ixingzhi.archint.mine.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.ixingzhi.archint.mine.settings.SettingsDiffListener
import com.ixingzhi.archint.mine.settings.SettingsItemKeys

/**
 * 海外版账户设置差异实现
 *
 * 海外版特点：
 * 1. 有语言选项（差异项）
 * 2. 底部没有广告区域
 *
 * Created by xiedongdong on 2026/1/19
 */
object SettingsDiffImpl : SettingsDiffListener {

    /**
     * 获取设置项列表
     * 海外版包含：Account, Theme, Language, Logout
     */
    override fun getItems(): Map<String, ImageVector> {
        return mapOf(
            SettingsItemKeys.ACCOUNT to Icons.Filled.AccountCircle,
            SettingsItemKeys.THEME to Icons.Filled.DarkMode,
            SettingsItemKeys.LANGUAGE to Icons.Filled.Language,
            SettingsItemKeys.LOGOUT to Icons.Filled.ExitToApp
        )
    }

    /**
     * 底部广告区域
     * 海外版不显示广告
     */
    @Composable
    override fun getBottomADView() {
        // 海外版不显示广告，空实现
    }

}
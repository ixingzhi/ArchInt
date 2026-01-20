package com.ixingzhi.archint.mine.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 设置项常量
 * 用于标识不同的设置项
 */
object SettingsItemKeys {
    const val ACCOUNT = "Account"
    const val THEME = "Theme"
    const val LANGUAGE = "Language"
    const val LOGOUT = "Logout"
}

/**
 * 设置差异监听接口
 * 
 * 用于实现不同版本（国版/海外版）的设置项差异：
 * - 国版：没有语言选项，有广告区域
 * - 海外版：有语言选项，没有广告区域
 * 
 * Created by xiedongdong on 2026/1/19
 */
interface SettingsDiffListener {

    /**
     * 获取设置项列表
     * @return Map<设置项名称, 图标>
     */
    fun getItems(): Map<String, ImageVector>

    /**
     * 获取底部广告区域
     * 国版返回广告视图，海外版返回空视图
     */
    @Composable
    fun getBottomADView()
}
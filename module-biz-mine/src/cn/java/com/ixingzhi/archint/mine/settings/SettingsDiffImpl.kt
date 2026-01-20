package com.ixingzhi.archint.mine.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 国版账户设置差异实现
 *
 * 国版特点：
 * 1. 没有语言选项（差异项）
 * 2. 底部有广告区域
 *
 * Created by xiedongdong on 2026/1/19
 */
object SettingsDiffImpl : SettingsDiffListener {

    /**
     * 获取设置项列表
     * 国版包含：Account, Theme, Logout
     */
    override fun getItems(): Map<String, ImageVector> {
        return mapOf(
            SettingsItemKeys.ACCOUNT to Icons.Filled.AccountCircle,
            SettingsItemKeys.THEME to Icons.Filled.DarkMode,
            SettingsItemKeys.LOGOUT to Icons.Filled.ExitToApp
        )
    }

    /**
     * 底部广告区域
     * 国版显示广告，海外版不显示
     */
    @Composable
    override fun getBottomADView() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(bottom = 16.dp, top = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "底部固定AD区域",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
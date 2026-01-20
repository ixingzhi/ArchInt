package com.ixingzhi.archint.mine.settings

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.ixingzhi.archint.mine.api.route.SettingsLauncher

/**
 * Created by xiedongdong on 2026/1/19
 */
@Route(path = SettingsLauncher.LAUNCHER)
class SettingsLauncherImpl : SettingsLauncher {

    override fun init(context: Context) {}

    override fun startActivity(context: Context) {
        context.startActivity(SettingsActivity.createIntent(context))
    }

}
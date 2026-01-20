package com.ixingzhi.archint.mine.api.route

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider

/**
 * Created by xiedongdong on 2026/1/19
 */
interface SettingsLauncher : IProvider {

    companion object {
        const val LAUNCHER = "/mine/settings/SettingsActivity"
    }

    fun startActivity(context: Context)

}
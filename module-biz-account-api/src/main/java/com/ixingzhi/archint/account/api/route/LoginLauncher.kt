package com.ixingzhi.archint.account.api.route

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider

/**
 * Created by xiedongdong on 2026/1/19
 */
interface LoginLauncher : IProvider {

    companion object {
        const val LAUNCHER = "/account/login/LoginActivity"
    }

    fun startActivity(context: Context)

}
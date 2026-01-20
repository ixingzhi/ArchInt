package com.ixingzhi.archint.account.login

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.ixingzhi.archint.account.api.route.LoginLauncher

/**
 * Created by xiedongdong on 2026/1/19
 */
@Route(path = LoginLauncher.LAUNCHER)
class LoginLauncherImpl : LoginLauncher {

    override fun init(context: Context) {}

    override fun startActivity(context: Context) {
        context.startActivity(LoginActivity.createIntent(context))
    }

}
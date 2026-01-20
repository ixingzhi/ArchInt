package com.ixingzhi.archint.base

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter


/**
 * Created by xiedongdong on 2025/4/3
 */
class ArchIntApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            ARouter.openDebug()
            ARouter.openLog()
        }
        ARouter.init(this)
    }

}
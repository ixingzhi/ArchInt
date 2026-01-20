package com.ixingzhi.archint.base.router

import android.util.Log
import com.alibaba.android.arouter.launcher.ARouter

/**
 * Created by xiedongdong on 2026/1/19
 */
object Launcher {
    private const val TAG = "Launcher"

    fun <T> navigation(launcher: Class<out T>?): T? {
        return try {
            ARouter.getInstance().navigation(launcher)
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed for ${launcher?.simpleName}", e)
            null
        }
    }
}
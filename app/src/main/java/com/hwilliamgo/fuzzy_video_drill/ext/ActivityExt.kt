package com.hwilliamgo.fuzzy_video_drill.ext

import android.app.Activity
import com.william.fastpermisssion.FastPermission
import com.william.fastpermisssion.OnPermissionCallback
import java.util.*

/**
 * @date: 2021/9/20
 * @author: HWilliamgo
 * @description: Activity常用的拓展函数
 */

/**
 * 请求权限，并只处理请求成功的回调
 */
fun Activity.requestPermission(vararg elements: String, onSuccess: () -> Unit) {
    FastPermission.getInstance().start(this, arrayListOf(
        *elements
    ), object : OnPermissionCallback {
        override fun onAllGranted() {
            onSuccess()
        }

        override fun onGranted(grantedPermissions: ArrayList<String>?) {
        }

        override fun onDenied(deniedPermissions: ArrayList<String>?) {
        }

        override fun onDeniedForever(deniedForeverP: ArrayList<String>?) {
        }
    })
}
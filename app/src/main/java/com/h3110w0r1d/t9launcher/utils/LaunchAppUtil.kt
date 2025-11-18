package com.h3110w0r1d.t9launcher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Vibrator
import android.os.VibrationEffect
import android.widget.Toast
import com.h3110w0r1d.t9launcher.R

object LaunchAppUtil {

    /**
     * 通过包名启动应用
     * @param context 上下文
     * @param packageName 目标应用的包名
     * @param onError 启动失败时的错误回调（可选）
     * @return Boolean 是否成功启动
     */
    fun launchAppByPackageName(
        context: Context,
        packageName: String,
        onError: ((String) -> Unit)? = null
    ): Boolean {
        var reason: String? = null
        return try {
            // 基础参数校验
            if (packageName.isBlank()) {
              //  handleError("Package name cannot be empty", onError)
                return false
            }

            val packageManager = context.packageManager

            // 检查包是否存在
            if (!isPackageExist(packageManager, packageName)) {
               // handleError("Application not installed", onError)
                return false
            }

            // 获取启动 Intent
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                // 添加标志位避免启动新任务时的潜在问题
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
               reason = context.getString(R.string.app_start_failed_params)
                onError?.invoke(reason)
                false
            }
        } catch (e: Exception) {
            reason = context.getString(R.string.app_start_failed)
            onError?.invoke(reason)
            false
        }
    }

    /**
     * 检查应用是否安装
     */
    private fun isPackageExist(pm: PackageManager, packageName: String): Boolean {
        return try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
 
    fun vibrateWeak(context: Context) {
     vibrateWeak(context)
    }

    /**
     * 执行振动
     * @param context 上下文
     */
    fun vibrate(context: Context, duration: Long = 15) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                // 使用较短的振动时间（15毫秒）和较低的强度来实现弱振动效果
                val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            }
        } catch (e: Exception) {
            // 捕获所有可能的异常，避免崩溃
            // 可以考虑添加日志记录
        }
    }
 
}

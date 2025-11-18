package com.h3110w0r1d.t9launcher.data.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.core.net.toUri
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.utils.LaunchAppUtil
import com.h3110w0r1d.t9launcher.utils.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import rikka.shizuku.ShizukuBinderWrapper

class AppInfo(
    val className: String,
    val packageName: String,
    var appName: String,
    var startCount: Int,
    var appIcon: ImageBitmap,
    var isSystemApp: Boolean,
    var searchData: List<List<String>>,
    var isEnabled: Boolean = true, // 添加应用启用状态字段
) {
    var matchRate: Float = 0f
    private val _matchRange: MutableStateFlow<Pair<Int, Int>> = MutableStateFlow(Pair(0, 0))
    val matchRange: StateFlow<Pair<Int, Int>> = _matchRange
    private val _annotatedName: MutableStateFlow<AnnotatedString> =
        MutableStateFlow(
            androidx.compose.ui.text
                .AnnotatedString(appName),
        )
    val annotatedName: StateFlow<AnnotatedString> = _annotatedName

    class SortByMatchRate : Comparator<AppInfo> {
        override fun compare(
            p0: AppInfo,
            p1: AppInfo,
        ): Int {
            if (p0.matchRate == p1.matchRate) {
                return 0
            }
            return if (p0.matchRate > p1.matchRate) -1 else 1
        }
    }

    class SortByStartCount : Comparator<AppInfo> {
        override fun compare(
            p0: AppInfo,
            p1: AppInfo,
        ): Int = p1.startCount - p0.startCount
    }

    fun setMatchRange(
        start: Int,
        end: Int,
    ) {
        if (start == 0 && end == 0) {
            return
        }
        _matchRange.value = Pair(start, end)
    }

    fun updateAnnotatedName(highlightColor: Color) {
        _annotatedName.value =
            buildAnnotatedString {
                for (i in searchData.indices) {
                    if (matchRange.value.first <= i && i < matchRange.value.second) {
                        withStyle(
                            style =
                                SpanStyle(
                                    color = highlightColor,
                                    fontWeight = FontWeight.Companion.SemiBold,
                                ),
                        ) {
                            append(searchData[i].last())
                        }
                    } else {
                        append(searchData[i].last())
                    }
                }
            }
    }

    fun start(ctx: Context, launchByCP: Boolean = false): Boolean {
        if(launchByCP){
                // 使用原始的ComponentName启动方案
            val componentName = ComponentName(packageName, className)
           val intent = Intent(Intent.ACTION_MAIN).apply {
                component = componentName
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

             try {
                ctx.startActivity(intent)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                // 显示启动失败的Toast提示
                Toast.makeText(ctx, ctx.getString(R.string.app_start_failed), Toast.LENGTH_SHORT).show()
                return false
            }
            
            return false
        }
 
    return LaunchAppUtil.launchAppByPackageName(ctx, packageName) { reason ->
     Toast.makeText(ctx, reason, Toast.LENGTH_SHORT).show()
    } 
 }

    fun detail(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = "package:$packageName".toUri()
        context.startActivity(intent)
    }

    fun uninstall(context: Context) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = "package:$packageName".toUri()
        context.startActivity(intent)
    }

    fun copyPackageName(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", packageName)
        clipboard.setPrimaryClip(clip)
    }

    fun componentId(): String = "$packageName/$className"
    
    /**
     * 启动应用辅助方法
     * 首先检查应用是否启用，如果启用则直接执行启动
     * 如果未启用，则尝试通过Shizuku启用应用后再启动
     * @param context 上下文
     * @return 是否启动成功
     */
    fun startHelperPack(context: Context): Boolean {
        // 首先检查应用是否启用
        val isAppEnabled = ShizukuManager.checkAppEnabled(context, packageName)
        if (isAppEnabled) {
            // 如果应用已启用，直接调用现有的start方法
            return start(context)
        }
        
        // 应用未启用的逻辑实现 - 使用Kotlin协程替代直接的Thread
        // 初始化ShizukuManager
        val shizukuManager = ShizukuManager.getInstance()
                shizukuManager.initialize(context)
        
        // 检查是否有Shizuku权限
        if (!shizukuManager.hasPermission) {
            // 没有权限，显示Toast提示
            Toast.makeText(context, R.string.need_shizuku_permission, Toast.LENGTH_SHORT).show()
            return false
        }
        
        // 使用Kotlin协程在后台线程执行操作
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 在IO线程调用 setAppState 方法启用应用
                val enableSuccess = shizukuManager.setAppState(packageName, true)
                
                // 切换到主线程显示操作结果
                withContext(Dispatchers.Main) {
                    if (enableSuccess) {
                        // 启用成功，尝试启动应用
                       // Toast.makeText(context, R.string.app_enabled_success, Toast.LENGTH_SHORT).show()
                        // start(context)
                        LaunchAppUtil.launchAppByPackageName(context, packageName)
                    } else {
                        // 启用失败
                        Toast.makeText(context, R.string.app_start_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 在主线程显示异常信息
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.operation_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        return true // 立即返回false，因为实际操作在协程中异步执行
    }
 
}

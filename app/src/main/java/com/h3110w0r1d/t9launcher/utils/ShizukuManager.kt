package com.h3110w0r1d.t9launcher.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.IPackageManager
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import com.h3110w0r1d.t9launcher.BuildConfig
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnBinderDeadListener
import rikka.shizuku.Shizuku.OnBinderReceivedListener
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;
import java.util.concurrent.atomic.AtomicBoolean
import org.lsposed.hiddenapibypass.HiddenApiBypass
import com.h3110w0r1d.t9launcher.utils.OSTarget

/**
 * Shizuku管理器 - 处理Shizuku API的初始化和权限请求
 */
class ShizukuManager private constructor() {

    private val SHELL_NAME = "com.android.shell"
    val myUserId get() = android.os.Process.myUserHandle().hashCode()

    // 权限请求码
    private val PERMISSION_REQUEST_CODE = 1001
    
    // 单例实例
    companion object {
        @Volatile
        private var instance: ShizukuManager? = null
        
        fun getInstance(): ShizukuManager {
            return instance ?: synchronized(this) {
                instance ?: ShizukuManager().also { instance = it }
            }
        } 
 
        /**
         * 检查应用是否已启用（统一方法）
         * @param context 上下文
         * @param packageName 应用包名
         * @return 应用是否已启用
         */
        fun checkAppEnabled(context: Context, packageName: String): Boolean {
            try {
                val packageManager = context.packageManager
                val enabledSetting = packageManager.getApplicationEnabledSetting(packageName)
                return enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || 
                       enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT 
                     //  || enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
            } catch (e: Exception) {
                e.printStackTrace()
                return true // 默认假设应用已启用
            }
        }
    }
     
    // Shizuku连接状态
    private val _isInitialized = AtomicBoolean(false)
    val isInitialized: Boolean
        get() = _isInitialized.get()
    
    // 是否有Shizuku权限
    private val _hasPermission = AtomicBoolean(false)
    val hasPermission: Boolean
        get() = _hasPermission.get()
    
    // 权限结果监听器
    private val permissionResultListeners = mutableSetOf<(Boolean) -> Unit>()
    
    // Binder相关监听器
    private val binderReceivedListener = OnBinderReceivedListener {
        checkAndRequestPermission()
    }
    
    private val binderDeadListener = OnBinderDeadListener {
        _hasPermission.set(false)
        notifyPermissionResultListeners(false)
    }
    
    private val requestPermissionResultListener = OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            _hasPermission.set(granted)
            notifyPermissionResultListeners(granted)
        }
    }
    
    /**
     * 初始化Shizuku
     * @param context 上下文
     * @param requestPermission 是否在初始化时请求权限，默认为false
     */
    fun initialize(context: Context, requestPermission: Boolean = true) {
        if (_isInitialized.getAndSet(true)) return
        
        // 注册Shizuku监听器
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
        
        // 只有在requestPermission为true时才检查和请求权限
        if (Shizuku.pingBinder()) {
            checkAndRequestPermission(if (requestPermission) false else true)
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        if (!_isInitialized.get()) return
        
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        
        permissionResultListeners.clear()
        _isInitialized.set(false)
        _hasPermission.set(false)
    }
    
    /**
     * 检查并请求Shizuku权限
     */
    fun checkAndRequestPermission(checkOnly: Boolean = false): Boolean {
        if (!Shizuku.pingBinder()) {
            return false
        }
        
        if (Shizuku.isPreV11()) {
            // 不支持v11之前的版本
            return false
        }
        
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            _hasPermission.set(true)
            notifyPermissionResultListeners(true)
            return true
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            // 用户选择了"拒绝并不再询问"
            _hasPermission.set(false)
            notifyPermissionResultListeners(false)
            return false
        } else {
            // 请求权限
            if (!checkOnly) {
                Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
            }
            return false
        }
    }
    
    /**
     * 添加权限结果监听器
     */
    fun addPermissionResultListener(listener: (Boolean) -> Unit) {
        permissionResultListeners.add(listener)
        // 立即通知当前状态
        listener.invoke(_hasPermission.get())
    }
    
    /**
     * 移除权限结果监听器
     */
    fun removePermissionResultListener(listener: (Boolean) -> Unit) {
        permissionResultListeners.remove(listener)
    }
    
    /**
     * 通知所有权限结果监听器
     */
    private fun notifyPermissionResultListeners(granted: Boolean) {
        permissionResultListeners.forEach { it.invoke(granted) }
    }
    
    /**
     * 获取Shizuku运行模式（ADB或ROOT）
     */
    fun getShizukuMode(): ShizukuMode {
        if (!Shizuku.pingBinder()) {
            return ShizukuMode.NOT_AVAILABLE
        }
        
        return when (Shizuku.getUid()) {
            0 -> ShizukuMode.ROOT
            2000 -> ShizukuMode.ADB
            else -> ShizukuMode.UNKNOWN
        }
    }
    
    /**
     * 检查Shizuku是否可用
     */
    fun isShizukuAvailable(): Boolean {
        return Shizuku.pingBinder()
    }
    
    /**
     * 设置应用启用状态
     * @param packageName 应用包名
     * @param disabled 是否禁用
     * @return 操作是否成功
     */
    fun setAppState(packageName: String, state: Boolean): Boolean {
          
        runCatching {
            val pm = asInterface("android.content.pm.IPackageManager", "package")
            val newState = if(state) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
            pm::class.java.getMethod(
                "setApplicationEnabledSetting",
                String::class.java,
                Int::class.java,
                Int::class.java,
                Int::class.java,
                String::class.java
            ).invoke(pm, packageName, newState, 0, myUserId, SHELL_NAME)
        }.onFailure {
           return false
        }

       return true
    }
    
    /**
     * 强制停止应用
     * @param packageName 应用包名
     */
    fun forceStopApp(packageName: String): Boolean = runCatching {
        asInterface("android.app.IActivityManager", Context.ACTIVITY_SERVICE).let {
            if (OSTarget.P) HiddenApiBypass.invoke(
                it::class.java, it, "forceStopPackage", packageName, myUserId
            ) else it::class.java.getMethod(
                "forceStopPackage", String::class.java, Int::class.java
            ).invoke(
                it, packageName, myUserId
            )
        }
        true
    }.getOrElse {
        false
    }
    
    private fun asInterface(className: String, original: IBinder): Any = Class.forName("$className\$Stub").run {
        if (OSTarget.P) HiddenApiBypass.invoke(this, null, "asInterface", ShizukuBinderWrapper(original))
        else getMethod("asInterface", IBinder::class.java).invoke(null, ShizukuBinderWrapper(original))
    }

    private fun asInterface(className: String, serviceName: String): Any =
        asInterface(className, SystemServiceHelper.getSystemService(serviceName))
}


/**
 * Shizuku运行模式
 */
enum class ShizukuMode {
    NOT_AVAILABLE,
    ROOT,
    ADB,
    UNKNOWN
}

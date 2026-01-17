package com.h3110w0r1d.t9launcher

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.h3110w0r1d.t9launcher.utils.ShizukuManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    // 保存PackageReceiver实例
    private val packageReceiver: com.h3110w0r1d.t9launcher.PackageReceiver by lazy { com.h3110w0r1d.t9launcher.PackageReceiver() }
    // 标记接收器是否已注册
    private var isPackageReceiverRegistered = false

    override fun onCreate() {
        super.onCreate()
        
        // 只有在PackageReceiver启用的状态下才注册
        if (isPackageReceiverEnabled()) {
            registerPackageReceiver()
        }
        
        // 初始化Shizuku
        initShizuku()
    }

    override fun onTerminate() {
        super.onTerminate()
        
        // 只有在接收器已注册时才注销
        if (isPackageReceiverRegistered) {
            unregisterReceiver(packageReceiver)
            isPackageReceiverRegistered = false
        }
        
        // 清理Shizuku资源
        ShizukuManager.getInstance().cleanup()
    }

    private fun registerPackageReceiver() {
        if (isPackageReceiverRegistered) return
        
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        intentFilter.addDataScheme("package")
        
        try {
            registerReceiver(packageReceiver, intentFilter)
            isPackageReceiverRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun unregisterPackageReceiver() {
        if (!isPackageReceiverRegistered) return
        
        try {
            unregisterReceiver(packageReceiver)
            isPackageReceiverRegistered = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 检查PackageReceiver是否启用
     */
    fun isPackageReceiverEnabled(): Boolean {
        val componentName = ComponentName(this, com.h3110w0r1d.t9launcher.PackageReceiver::class.java)
        val status = packageManager.getComponentEnabledSetting(componentName)
        return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }
    
    /**
     * 启用或禁用PackageReceiver
     */
    fun setPackageReceiverEnabled(enabled: Boolean) {
        val componentName = ComponentName(packageName, com.h3110w0r1d.t9launcher.PackageReceiver::class.java.name)
        val newState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        
        packageManager.setComponentEnabledSetting(
            componentName,
            newState,
            PackageManager.DONT_KILL_APP
        )
        
        // 根据新状态更新注册状态
        if (enabled && !isPackageReceiverRegistered) {
            registerPackageReceiver()
        } else if (!enabled && isPackageReceiverRegistered) {
            unregisterPackageReceiver()
        }
    }
    
    private fun initShizuku() {
        // 检查Shizuku包是否存在，仅在包存在时初始化
        ShizukuManager.getInstance().initialize(this, false)
    }
}

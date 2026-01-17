package com.h3110w0r1d.t9launcher.activity

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.utils.LaunchAppUtil
import com.h3110w0r1d.t9launcher.utils.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

// 定义为包级常量，方便 widget 直接引用
const val ACTION_ENABLE = "com.h3110w0r1d.t9launcher.action.ENABLE"
const val ACTION_DISABLE = "com.h3110w0r1d.t9launcher.action.DISABLE"
const val EXTRA_LAUNCH_FROM_WIDGET = "launchFromWidget"
const val EXTRA_OPERATION_4_SYSTEM_APPS = "operation4SystemApps"

open class BatchOperationsActivity : Activity() {
 
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "batch_operations_channel"
        private const val NOTIFICATION_ID = 12345
        
       // private const val EXTRA_PACKAGE_NAMES = "packageNames"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         
        // 从Intent读取参数
        val operation4SystemApps = intent.getBooleanExtra(EXTRA_OPERATION_4_SYSTEM_APPS, false)
        val launchFromWidget = intent.getBooleanExtra(EXTRA_LAUNCH_FROM_WIDGET, false)
        
        // 读取preference
        GlobalScope.launch(Dispatchers.IO) {
            val prefKey = if (operation4SystemApps) "batch_operations_4_system_apps" else "batch_operations_4_user_apps"
            val preferences = batchOperationsDataStore.data.first()
            val checkedPackagesString = preferences[stringPreferencesKey(prefKey)] ?: ""
            
            // 解析应用列表
            val batchOperation2AppsList = checkedPackagesString.split(",").filter { it.isNotEmpty() }
            val emptyCheck = batchOperation2AppsList.isEmpty()
            
            withContext(Dispatchers.Main) {
                if (emptyCheck) {
                    Toast.makeText(this@BatchOperationsActivity, R.string.current_not_configured_app_list_cannot_perform_batch_operation, Toast.LENGTH_SHORT).show()
                    finish()
                    return@withContext
                }
                
                if (launchFromWidget) {
                    // 从Widget启动，直接执行操作
                    val action = intent.action
                    when (action) {
                        ACTION_ENABLE -> performBatchOperation(true, batchOperation2AppsList)
                        ACTION_DISABLE -> performBatchOperation(false, batchOperation2AppsList)
                        else -> {
                            // 如果没有有效的ACTION，显示通知让用户选择
                            showBatchOperationNotification(batchOperation2AppsList.size, operation4SystemApps)
                        }
                    }
                } else {
                    // 正常启动，显示通知让用户选择
                    showBatchOperationNotification(batchOperation2AppsList.size, operation4SystemApps)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleNotificationAction(intent)
    }

    private fun handleNotificationAction(intent: Intent?) {
        intent?.let {
            // 设置当前Intent为新的intent，以便onCreate可以处理它
            setIntent(it)
            // 重新处理intent
            val operation4SystemApps = it.getBooleanExtra(EXTRA_OPERATION_4_SYSTEM_APPS, false)
            val launchFromWidget = true // 从通知点击启动，视为从widget启动
            
            // 读取preference
            GlobalScope.launch(Dispatchers.IO) {
                val prefKey = if (operation4SystemApps) "batch_operations_4_system_apps" else "batch_operations_4_user_apps"
                val preferences = batchOperationsDataStore.data.first()
                val checkedPackagesString = preferences[stringPreferencesKey(prefKey)] ?: ""
                
                // 解析应用列表
                val batchOperation2AppsList = checkedPackagesString.split(",").filter { it.isNotEmpty() }
                val emptyCheck = batchOperation2AppsList.isEmpty()
                
                withContext(Dispatchers.Main) {
                    if (emptyCheck) {
                        Toast.makeText(this@BatchOperationsActivity, R.string.current_not_configured_app_list_cannot_perform_batch_operation, Toast.LENGTH_SHORT).show()
                        finish()
                        return@withContext
                    }
                    
                    // 直接执行操作
                    val action = it.action
                    when (action) {
                        ACTION_ENABLE -> performBatchOperation(true, batchOperation2AppsList)
                        ACTION_DISABLE -> performBatchOperation(false, batchOperation2AppsList)
                    }
                }
            }
        }
    }

    private fun showBatchOperationNotification(count: Int, isSystem: Boolean) {
        // 检查是否有通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.areNotificationsEnabled()) {
                
                LaunchAppUtil.vibrate(this, 300)
                Toast.makeText(this, R.string.need_notification_permission, Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
        
        // 创建通知渠道
        createNotificationChannel()
        
        val appType = if (isSystem) getString(R.string.system_apps) else getString(R.string.user_apps)
        val contentText = String.format(getString(R.string.please_select_batch_operation_for_d_apps), count, appType)
        
        // 创建启用按钮的PendingIntent
        val enableIntent = Intent(this, BatchOperationsActivity::class.java).apply {
            action = ACTION_ENABLE
            putExtra(EXTRA_LAUNCH_FROM_WIDGET, true)
            putExtra(EXTRA_OPERATION_4_SYSTEM_APPS, isSystem)
        }
        val enablePendingIntent = PendingIntent.getActivity(
            this,
            0,
            enableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 创建禁用按钮的PendingIntent
        val disableIntent = Intent(this, BatchOperationsActivity::class.java).apply {
            action = ACTION_DISABLE
            putExtra(EXTRA_LAUNCH_FROM_WIDGET, true)
            putExtra(EXTRA_OPERATION_4_SYSTEM_APPS, isSystem)
        }
        val disablePendingIntent = PendingIntent.getActivity(
            this,
            1,
            disableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 构建通知
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.batch_operations))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_batch_operations_enable, 
                getString(R.string.enable),
                enablePendingIntent
            )
            .addAction(
                R.drawable.ic_batch_operations_disable, 
                getString(R.string.disable),
                disablePendingIntent
            )
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
        
        // 显示通知
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
        
        // 退出Activity
        finish()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.batch_operations)
            val descriptionText = getString(R.string.batch_operations_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun performBatchOperation(newState: Boolean, packageNames: List<String>) {
        // 取消通知
        with(NotificationManagerCompat.from(this)) {
            cancel(NOTIFICATION_ID)
        }
        
        // 初始化ShizukuManager
        val shizukuManager = ShizukuManager.getInstance()
        shizukuManager.initialize(this)
        
        // 检查是否有Shizuku权限
        val shizukuInit = shizukuManager.hasPermission
        
        if (!shizukuInit) {
            Toast.makeText(this, R.string.need_shizuku_permission, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // 在后台执行批量操作
        GlobalScope.launch(Dispatchers.IO) {
            var operationCount = 0
            val checkRequired = true
            
            packageNames.forEach { packageName ->
                if (checkRequired) {
                    if (newState) {
                        if (!ShizukuManager.checkAppEnabled(applicationContext, packageName)) {
                            if (shizukuManager.setAppState(packageName, true)) {
                                operationCount++
                            }
                        }
                    } else {
                        if (ShizukuManager.checkAppEnabled(applicationContext, packageName)) {
                            if (shizukuManager.setAppState(packageName, false)) {
                                operationCount++
                            }
                        }
                    }
                } else {
                    if (shizukuManager.setAppState(packageName, newState)) {
                        operationCount++
                    }
                }
            }
            
            // 发送操作结果通知
            withContext(Dispatchers.Main) {
                val resultText = String.format(getString(R.string.batch_operation_completed_d_apps_affected), if (newState) getString(R.string.enable) else getString(R.string.disable), operationCount)
                Toast.makeText(this@BatchOperationsActivity, resultText, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
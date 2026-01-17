package com.h3110w0r1d.t9launcher.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews

import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.ui.widget.LargeWidgetProviderDark
import com.h3110w0r1d.t9launcher.ui.widget.LargeWidgetProvider


/**
 * Activity，用于显示Widget图标位置选择对话框
 * 由于AppWidgetProvider无法直接显示对话框，需要通过这个Activity来实现
 */
class IconPositionActivity : Activity() {
    
    private var darkMode: Boolean = false
    
    companion object {
        // 静态常量定义
        private const val PREFS_NAME = "widget_prefs"
        private const val KEY_ICON_POSITION = "icon_position"
        private const val KEY_ICON_POSITION_DARK = "icon_position_dark"
        
        // 静态方法，供Widget Provider调用
        fun getCurrentIconPosition(context: Context, dark: Boolean): String {
            val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return preferences.getString(if (dark) KEY_ICON_POSITION_DARK else KEY_ICON_POSITION, "left") ?: "left"
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 接收Intent中的dark参数
        darkMode = intent.getBooleanExtra("dark", false)
        
        // 不需要设置布局，直接显示对话框
        // 延迟一点显示对话框，确保UI完全初始化
        window.decorView.postDelayed({            try {
                showIconPositionDialog()
            } catch (e: Exception) {
                // 如果出错，确保Activity能关闭
                finish()
            }
        }, 300) // 300毫秒延迟
    }
    
    private fun showIconPositionDialog() {
        // 获取当前保存的位置
        val currentPosition = getCurrentIconPosition(darkMode)
        
        // 创建对话框，使用系统主题
        val builder = AlertDialog.Builder(this, if (darkMode) AlertDialog.THEME_DEVICE_DEFAULT_DARK else AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
            .setTitle(R.string.icon_position_title)
            .setMessage(R.string.icon_position_message)
        
        // 添加三个按钮：左、中、右
        builder.setPositiveButton(R.string.position_left) { dialog, _ ->
            savePositionAndUpdate("left", darkMode)
            dialog.dismiss()
        }
        
        builder.setNeutralButton(R.string.position_center) { dialog, _ ->
            savePositionAndUpdate("center", darkMode)
            dialog.dismiss()
        }
        
        builder.setNegativeButton(R.string.position_right) { dialog, _ ->
            savePositionAndUpdate("right", darkMode)
            dialog.dismiss()
        }
        
        // 设置对话框关闭监听器
        val dialog = builder.create()
        dialog.setOnDismissListener { 
            // 确保对话框关闭后Activity也关闭
            finish()
        }
        
        dialog.show()
    }
    
    private fun getCurrentIconPosition(dark: Boolean): String {
        // 调用静态方法，保持逻辑一致性
        return getCurrentIconPosition(this, dark)
    }
    
    private fun savePositionAndUpdate(position: String, dark: Boolean) {
        try {
            // 保存到SharedPreferences
            val preferences = getSharedPreferences(Companion.PREFS_NAME, Context.MODE_PRIVATE)
            preferences.edit().putString(if (dark) Companion.KEY_ICON_POSITION_DARK else Companion.KEY_ICON_POSITION, position).apply()
            
            // 更新Widget图标
            updateWidgetIcons(position, darkMode)
            
        } finally {
            // 无论成功与否，都结束Activity
            finish()
        }
    }
    
    private fun updateWidgetIcons(position: String, dark: Boolean) {
        try {
            val showLeft = position == "left"
            val showCenter = position == "center"
            val showRight = position == "right"
            
            // 更新所有Widget实例
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val componentName = ComponentName(this, if (dark) LargeWidgetProviderDark::class.java else LargeWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(packageName, R.layout.widget_large)
                
                // 更新可见性
                views.setViewVisibility(R.id.widget_icon_left, if (showLeft) View.VISIBLE else View.GONE)
                views.setViewVisibility(R.id.widget_icon_center, if (showCenter) View.VISIBLE else View.GONE)
                views.setViewVisibility(R.id.widget_icon_right, if (showRight) View.VISIBLE else View.GONE)
                
                // 更新Widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        } catch (e: Exception) {
            // 错误已处理，静默失败
        }
    }
    
    override fun onBackPressed() {
        // 拦截返回键，确保Activity正确关闭
        finish()
    }
}
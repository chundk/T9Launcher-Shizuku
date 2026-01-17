package com.h3110w0r1d.t9launcher.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.activity.MainActivity
import com.h3110w0r1d.t9launcher.ui.dialog.IconPositionActivity

/**
 * 大尺寸Widget的Provider类
 * 负责创建、更新Widget并处理点击事件
 */
class LargeWidgetProvider : AppWidgetProvider() {
    
    companion object {
        // 常量定义
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 更新所有大尺寸Widget实例
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // 设置Widget的点击事件，启动MainActivity
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            123, // 使用不同的请求码避免与其他Widget冲突
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 获取Widget的远程视图，使用新的深色背景布局
        val views = RemoteViews(context.packageName, R.layout.widget_large)
        
        // 设置Widget的点击事件
        views.setOnClickPendingIntent(R.id.widget_icon_left, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_icon_center, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_icon_right, pendingIntent)
        //views.setOnClickPendingIntent(android.R.id.background, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_shape_bg, pendingIntent)

        // 获取保存的图标位置设置
        val position = IconPositionActivity.getCurrentIconPosition(context, false)
        val showLeft = position == "left"
        val showCenter = position == "center"
        val showRight = position == "right"
        
        // 更新图标的可见性
        views.setViewVisibility(R.id.widget_icon_left, if (showLeft) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.widget_icon_center, if (showCenter) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.widget_icon_right, if (showRight) View.VISIBLE else View.GONE)

        // 更新Widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Widget首次创建时调用
        
        // 获取保存的图标位置设置（深色模式）
        val position = IconPositionActivity.getCurrentIconPosition(context, false)
        
        // 根据位置设置确定图标的可见性
        val showLeft = position == "left"
        val showCenter = position == "center"
        val showRight = position == "right"
        
        // 获取Widget管理器和所有Widget ID
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, LargeWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        // 调用封装的方法更新图标可见性
        updateIconVisibility(context, appWidgetManager, appWidgetIds, showLeft, showCenter, showRight)
    }

    private fun updateIconVisibility(
        context: Context, 
        appWidgetManager: AppWidgetManager, 
        appWidgetIds: IntArray,
        showLeft: Boolean, 
        showCenter: Boolean, 
        showRight: Boolean
    ) {
        // 更新Widget实例的图标可见性
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_large)
            
            // 更新图标的可见性
            views.setViewVisibility(R.id.widget_icon_left, if (showLeft) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.widget_icon_center, if (showCenter) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.widget_icon_right, if (showRight) View.VISIBLE else View.GONE)
            
            // 更新Widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
    
    override fun onDisabled(context: Context) {
        // 最后一个Widget实例被删除时调用
    }
}
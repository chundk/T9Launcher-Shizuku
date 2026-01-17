package com.h3110w0r1d.t9launcher.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.activity.MainActivity

/**
 * 小尺寸Widget的Provider类
 * 负责创建、更新Widget并处理点击事件
 */
class SmallWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 更新所有小尺寸Widget实例
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
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 获取Widget的远程视图
        val views = RemoteViews(context.packageName, R.layout.widget_small)
        
        // 设置Widget的点击事件
        views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent)
        views.setOnClickPendingIntent(android.R.id.background, pendingIntent)
         
        // 更新Widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onEnabled(context: Context) {
        // Widget首次创建时调用
    }

    override fun onDisabled(context: Context) {
        // 最后一个Widget实例被删除时调用
    }
}
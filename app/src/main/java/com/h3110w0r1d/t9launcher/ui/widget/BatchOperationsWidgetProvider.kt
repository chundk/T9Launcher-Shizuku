package com.h3110w0r1d.t9launcher.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.activity.ACTION_ENABLE
import com.h3110w0r1d.t9launcher.activity.ACTION_DISABLE
import com.h3110w0r1d.t9launcher.activity.BatchOperationsActivity
import com.h3110w0r1d.t9launcher.activity.EXTRA_LAUNCH_FROM_WIDGET
import com.h3110w0r1d.t9launcher.activity.EXTRA_OPERATION_4_SYSTEM_APPS

/**
 * 批量操作Widget的Provider类
 * 负责创建、更新Widget并处理点击事件
 */
class BatchOperationsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 更新所有批量操作Widget实例
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // 创建启用按钮的Intent
        val enableIntent = Intent(context, BatchOperationsActivity::class.java).apply {
            action = ACTION_ENABLE
            putExtra(EXTRA_LAUNCH_FROM_WIDGET, true)
            putExtra(EXTRA_OPERATION_4_SYSTEM_APPS, false)
        }
        val enablePendingIntent = PendingIntent.getActivity(
            context,
            0,
            enableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 创建禁用按钮的Intent
        val disableIntent = Intent(context, BatchOperationsActivity::class.java).apply {
            action = ACTION_DISABLE
            putExtra(EXTRA_LAUNCH_FROM_WIDGET, true)
            putExtra(EXTRA_OPERATION_4_SYSTEM_APPS, false)
        }
        val disablePendingIntent = PendingIntent.getActivity(
            context,
            1,
            disableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 获取Widget的远程视图
        val views = RemoteViews(context.packageName, R.layout.widget_batch_operations)
        
        // 设置启用按钮的点击事件
        views.setOnClickPendingIntent(R.id.btn_enable, enablePendingIntent)
        
        // 设置禁用按钮的点击事件
        views.setOnClickPendingIntent(R.id.btn_disable, disablePendingIntent)
        
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
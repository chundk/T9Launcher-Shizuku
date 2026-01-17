package com.h3110w0r1d.t9launcher.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.activity.ACTION_DISABLE
import com.h3110w0r1d.t9launcher.activity.ACTION_ENABLE
import com.h3110w0r1d.t9launcher.activity.BatchOperations4SystemOnlyActivity
import com.h3110w0r1d.t9launcher.activity.BatchOperationsSystemConfirmActivity

class BatchOperationsSystemWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_batch_operations_system)

            // Setup enable button intent
            val enableIntent = Intent(context, BatchOperationsSystemConfirmActivity::class.java).apply {
                putExtra(BatchOperationsSystemConfirmActivity.EXTRA_ACTION, ACTION_ENABLE)
                putExtra(BatchOperationsSystemConfirmActivity.EXTRA_LAUNCH_FROM_WIDGET, true)
            }
            val enablePendingIntent = PendingIntent.getActivity(
                context,
                0,
                enableIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.enable_button, enablePendingIntent)

            // Setup disable button intent
            val disableIntent = Intent(context, BatchOperationsSystemConfirmActivity::class.java).apply {
                putExtra(BatchOperationsSystemConfirmActivity.EXTRA_ACTION, ACTION_DISABLE)
                putExtra(BatchOperationsSystemConfirmActivity.EXTRA_LAUNCH_FROM_WIDGET, true)
            }
            val disablePendingIntent = PendingIntent.getActivity(
                context,
                1,
                disableIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.disable_button, disablePendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
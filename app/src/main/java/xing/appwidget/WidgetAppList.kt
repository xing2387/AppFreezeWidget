package xing.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import xing.appwidget.service.StackWidgetService
import xing.appwidget.storage.SharedPreferenceHelper

class WidgetAppList : WidgetBase() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (i in appWidgetIds.indices) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i])
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            SharedPreferenceHelper.deletePackageNameListPref(context, appWidgetId)
        }
    }

    companion object {

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) { //设置图标Adapter

            val intent = Intent(context, StackWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            val remoteViewRoot = RemoteViews(context.packageName, R.layout.layout_widget_app_list)
            remoteViewRoot.setRemoteAdapter(R.id.grid_view, intent)

            //设置点击后发送的PendingIntent
            val switchIntent = Intent(context, WidgetAppList::class.java)
            switchIntent.action = SWITCH_ACTION
            switchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val switchPendingIntent = PendingIntent.getBroadcast(context, 0, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViewRoot.setPendingIntentTemplate(R.id.grid_view, switchPendingIntent)

//            val editIntent = Intent(context, WidgetBase::class.java)
//            editIntent.action = EDIT_ACTION
//            editIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
//            val editPendingIntent = PendingIntent.getBroadcast(context, 0, editIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//            rv.setOnClickPendingIntent(R.id.btn_edit_mode, editPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, remoteViewRoot)
        }

    }

    override fun onReceive(context: Context, intent: Intent, appWidgetId: Int) {
        if (SWITCH_ACTION == intent.action) {
            val content = intent.getStringExtra(APP_LIST) ?: ""
            switchAppFreezeStatus(context, content.split(","), null)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.grid_view)
        }
    }

}
package xing.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.RemoteViews
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

            val rv = RemoteViews(context.packageName, R.layout.layout_widget_app_list)
            rv.setRemoteAdapter(R.id.grid_view, intent)

            //设置点击后发送的PendingIntent
            val switchIntent = Intent(context, WidgetBase::class.java)
            switchIntent.action = SWITCH_ACTION
            switchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val switchPendingIntent = PendingIntent.getBroadcast(context, 0, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setPendingIntentTemplate(R.id.grid_view, switchPendingIntent)

            val editIntent = Intent(context, WidgetBase::class.java)
            editIntent.action = EDIT_ACTION
            editIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val editPendingIntent = PendingIntent.getBroadcast(context, 0, editIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.btn_edit_mode, editPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, rv)
        }

    }

    override fun onReceive(context: Context, intent: Intent, appWidgetId: Int) {
        if (SWITCH_ACTION == intent.action) {
            val content = intent.getStringExtra(APP_LIST)
            val packageNames = content.split(",").toTypedArray()
            val sbCmd = StringBuilder()
            val pm = context.packageManager
            for (name in packageNames) { //                boolean isEnable = Utils.getEnableState(context, name);
                var applicationInfo: ApplicationInfo? = null
                try {
                    applicationInfo = pm.getApplicationInfo(name, PackageManager.GET_META_DATA)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
                if (applicationInfo != null) {
                    sbCmd.append("pm " + (if (!applicationInfo.enabled) "enable " else "disable ") + name + ";")
                    SharedPreferenceHelper.saveEnableState(context, name, !applicationInfo.enabled)
                }
            }
            rootCommand(sbCmd.toString())

            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.grid_view)
        } else if (EDIT_ACTION == intent.action) {
            val isEditMode = SharedPreferenceHelper.loadEditModePref(context, appWidgetId)
            SharedPreferenceHelper.setEditModePref(context, appWidgetId, !isEditMode)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.grid_view)
        }
        super.onReceive(context, intent)
    }

}
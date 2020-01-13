package xing.appwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import xing.appwidget.storage.SharedPreferenceHelper

class WidgetByLabel : WidgetBase() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // update each of the widgets with the remote adapter
        for (i in appWidgetIds.indices) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i])
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            SharedPreferenceHelper.deletePackageNameListPref(context, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent, appWidgetId: Int) {
        if (SWITCH_ACTION == intent.action) {
            val content = intent.getStringExtra(APP_LIST)
            Log.d("liujiaxing", "onReceive  $content")
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
            //            Toast.makeText(context, "Touched view " + content, Toast.LENGTH_SHORT).show();
            // It is the responsibility of the configuration activity to update the app widget
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
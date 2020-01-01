package xing.test.mywidget.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import xing.test.mywidget.R
import xing.test.mywidget.Utils
import xing.test.mywidget.configure.WidgetConfigureActivity
import java.io.DataOutputStream

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [MyAppWidgetConfigureActivity][WidgetConfigureActivity]
 */
class MyAppWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) { // update each of the widgets with the remote adapter
        for (i in appWidgetIds.indices) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i])
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) { // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            Utils.deletePackageNameListPref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) { // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) { // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        val mgr = AppWidgetManager.getInstance(context)
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
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
                    Utils.saveEnableState(context, name, !applicationInfo.enabled)
                }
            }
            rootCommand(sbCmd.toString())
            //            Toast.makeText(context, "Touched view " + content, Toast.LENGTH_SHORT).show();
// It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.grid_view)
        } else if (EDIT_ACTION == intent.action) {
            val isEditMode = Utils.loadEditModePref(context, appWidgetId)
            Utils.setEditModePref(context, appWidgetId, !isEditMode)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.grid_view)
        }
        super.onReceive(context, intent)
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                            appWidgetId: Int) { //设置图标Adapter
            val intent = Intent(context, StackWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            val rv = RemoteViews(context.packageName, R.layout.my_app_widget)
            rv.setRemoteAdapter(R.id.grid_view, intent)
            //设置点击后发送的PendingIntent
            val toastIntent = Intent(context, MyAppWidget::class.java)
            toastIntent.action = SWITCH_ACTION
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setPendingIntentTemplate(R.id.grid_view, toastPendingIntent)
            val editIntent = Intent(context, MyAppWidget::class.java)
            editIntent.action = EDIT_ACTION
            editIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val editPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.btn_edit_mode, editPendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, rv)
        }

        const val SWITCH_ACTION = "xing.test.mywidget.SWITCH_ACTION"
        const val EDIT_ACTION = "xing.test.mywidget.EDIT_ACTION"
        const val APP_LIST = "xing.test.mywidget.APP_LIST"
        fun rootCommand(command: String): Boolean {
            var process: Process? = null
            var os: DataOutputStream? = null
            try {
                process = Runtime.getRuntime().exec("su")
                os = DataOutputStream(process.outputStream)
                os.writeBytes(command + "\n")
                os.writeBytes("exit\n")
                os.flush()
                process.waitFor()
            } catch (e: Exception) {
                Log.d("*** DEBUG ***", "ROOT REE" + e.message)
                return false
            } finally {
                try {
                    os?.close()
                    process!!.destroy()
                } catch (e: Exception) {
                }
            }
            Log.d("*** DEBUG ***", "Root SUC ")
            return true
        }
    }
}
package xing.appwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import xing.appwidget.storage.SharedPreferenceHelper
import java.io.DataOutputStream

abstract class WidgetBase : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // update each of the widgets with the remote adapter
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
        onReceive(context, intent, appWidgetId)
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    abstract fun onReceive(context: Context, intent: Intent, appWidgetId: Int)

    companion object {

        const val SWITCH_ACTION = "xing.appwidget.SWITCH_ACTION"
        const val EDIT_ACTION = "xing.appwidget.EDIT_ACTION"
        const val APP_LIST = "xing.appwidget.APP_LIST"
        const val LABEL = "xing.appwidget.LABEL"

        fun switchAppFreezeStatus(context: Context, packageNames: Iterable<String>, isFreeze: Boolean?) {
            val sbCmd = StringBuilder()
            val pm = context.packageManager
            var isFreeze = isFreeze
            for (packageName in packageNames) {
                var applicationInfo: ApplicationInfo? = null
                try {
                    applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
                if (applicationInfo != null) {
                    if (isFreeze == null) {
                        isFreeze = applicationInfo.enabled
                    }
                    sbCmd.append("pm " + (if (!isFreeze) "enable " else "disable ") + packageName + "; ")
                    SharedPreferenceHelper.saveEnableState(context, packageName, !applicationInfo.enabled)
                }
            }
            Log.d("liujiaxing", "switchAppFreezeStatus $sbCmd")
            rootCommand(sbCmd.toString())
        }

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
package xing.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import xing.appwidget.storage.LabelStorageHelper
import xing.appwidget.storage.SharedPreferenceHelper

class WidgetByLabel : WidgetBase() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // update each of the widgets with the remote adapter
        for (i in appWidgetIds.indices) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i])
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            SharedPreferenceHelper.deleteAppWidgetLabelPref(context, appWidgetId)
        }
        super.onDeleted(context, appWidgetIds)
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

            val labelName = SharedPreferenceHelper.getAppWidgetLabelPref(context, appWidgetId)

            val remoteViewRoot = RemoteViews(context.packageName, R.layout.layout_widget_by_label)
            val enableStatus = SharedPreferenceHelper.getLabelEnableStatusPref(context, labelName)

            val actionStr = if (enableStatus) "冻结" else "解冻"
            remoteViewRoot.setTextViewText(R.id.tv_label_name, "$actionStr\n$labelName")

            //设置点击后发送的PendingIntent
            val switchIntent = Intent(context, WidgetByLabel::class.java)
            switchIntent.action = SWITCH_ACTION
            switchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            switchIntent.putExtra(LABEL, labelName)
            val switchPendingIntent = PendingIntent.getBroadcast(context, 0, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViewRoot.setOnClickPendingIntent(R.id.tv_label_name, switchPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, remoteViewRoot)
        }
    }

    override fun onReceive(context: Context, intent: Intent, appWidgetId: Int) {
        if (intent.action == SWITCH_ACTION) {
            val labelName = intent.getStringExtra(LABEL)
            if (labelName.isNullOrEmpty()) {
                return
            }

            LabelStorageHelper.init(context)
            val packageNameList = LabelStorageHelper.getPackageNameListByLabel(labelName)
            val enableStatus = SharedPreferenceHelper.getLabelEnableStatusPref(context, labelName)
            switchAppFreezeStatus(context, packageNameList, enableStatus)
            SharedPreferenceHelper.saveLabelEnableStatusPref(context, labelName, !enableStatus)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

}
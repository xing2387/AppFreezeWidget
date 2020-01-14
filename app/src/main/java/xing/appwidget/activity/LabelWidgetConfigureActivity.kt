package xing.appwidget.activity

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_label_widget_configure.*
import xing.appwidget.R
import xing.appwidget.WidgetByLabel
import xing.appwidget.storage.LabelStorageHelper
import xing.appwidget.storage.SharedPreferenceHelper

class LabelWidgetConfigureActivity : AppCompatActivity() {


    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_widget_configure)

        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: appWidgetId

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        LabelStorageHelper.init(this)
        LabelStorageHelper.labelSetLd.observe(this, Observer {
            val data = it
            if (data != null) {
                label_list.setData(it)
            }
        })

        tv_done.setOnClickListener {
            if (label_list.getSelectedLabels().isNullOrEmpty()) {
                return@setOnClickListener
            }
            val labelName = label_list.getSelectedLabels()[0]

            SharedPreferenceHelper.saveAppWidgetLabelPref(this, appWidgetId, labelName)

            val appWidgetManager = AppWidgetManager.getInstance(this)
            WidgetByLabel.updateAppWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }


}

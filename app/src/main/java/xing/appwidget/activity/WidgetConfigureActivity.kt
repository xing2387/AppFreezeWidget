package xing.appwidget.activity

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_widget_configure.*
import kotlinx.android.synthetic.main.activity_widget_configure.app_filter
import kotlinx.android.synthetic.main.layout_create_label.*
import xing.appwidget.R
import xing.appwidget.storage.SharedPreferenceHelper
import xing.appwidget.bean.AppInfo
import xing.appwidget.MyAppWidget
import xing.appwidget.bean.PackageFilterParam
import xing.appwidget.storage.AddPackageListTask
import xing.appwidget.widget.AppFilter

class WidgetConfigureActivity : Activity(), AddPackageListTask.OnDataRequestedCallback {

    companion object {
        private const val TAG = "MyAppWidgetConfigureAct"
        private const val EXTRA_TEST_APPWIDGET_ID = 10086
        fun startTest(context: Context) {
            val intent = Intent(context, WidgetConfigureActivity::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, EXTRA_TEST_APPWIDGET_ID)
            context.startActivity(intent)
        }
    }

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_widget_configure)
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)
        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        initView()
        initData()
    }

    private fun initData() {
        app_filter.setAppListView(rv_app_list)
        app_filter.setDataProvider(object : AppFilter.DataProvider {
            override fun request(param: PackageFilterParam?) {
                AddPackageListTask(this@WidgetConfigureActivity).execute(param)
            }
        })
        app_filter.setParam(PackageFilterParam(user = true, initWithGrid = true))
    }

    private fun initView() {
        btn_select_all.setOnClickListener { v -> rv_app_list.selectAll() }
        btn_un_select_all.setOnClickListener { rv_app_list.unSelectAll() }
        tv_btn_done.setOnClickListener {
            val context: Context = this@WidgetConfigureActivity
            // When the button is clicked, store the string locally
            SharedPreferenceHelper.savePackageNameListPref(context, appWidgetId, rv_app_list.getSelectedPackageName())
            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            MyAppWidget.Companion.updateAppWidget(context, appWidgetManager, appWidgetId)
            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    override fun onAppListGet(result: List<AppInfo>) {
        rv_app_list.setData(result)
    }

}
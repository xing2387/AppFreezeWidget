package xing.test.mywidget.configure

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_widget_configure.*
import xing.test.mywidget.BuildConfig
import xing.test.mywidget.R
import xing.test.mywidget.Utils
import xing.test.mywidget.appwidget.AppInfo
import xing.test.mywidget.appwidget.MyAppWidget
import java.util.*
import kotlin.collections.ArrayList

class WidgetConfigureActivity : Activity() {

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

    private var includeSystemApp = false
    private var includeUserApp = true

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

    private fun updateAppList() {
        val appInfoList = getPackageList(this, includeSystemApp, includeUserApp)
        rv_app_list.setData(appInfoList)
    }

    private fun initData() {
        updateAppList()
    }

    private fun initView() {
        btn_select_all.setOnClickListener { rv_app_list.selectAll() }
        btn_un_select_all.setOnClickListener { rv_app_list.unSelectAll() }
        tv_btn_done.setOnClickListener {
            val context: Context = this@WidgetConfigureActivity
            // When the button is clicked, store the string locally
            Utils.savePackageNameListPref(context, appWidgetId, rv_app_list.getSelectedPackageName())
            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            MyAppWidget.Companion.updateAppWidget(context, appWidgetManager, appWidgetId)
            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }

        cb_system.isChecked = includeSystemApp
        cb_user.isChecked = includeUserApp
        rv_app_list.layoutManager = GridLayoutManager(this, 4)
        cb_system.setOnCheckedChangeListener { buttonView, isChecked ->
            includeSystemApp = isChecked
            updateAppList()
        }
        cb_user.setOnCheckedChangeListener { buttonView, isChecked ->
            includeUserApp = isChecked
            updateAppList()
        }
    }

    private var packageInfoList: List<PackageInfo> = Collections.emptyList()

    private fun getPackageList(context: Context, includeSystemApp: Boolean, includeUserApp: Boolean): List<AppInfo> {
        val pm = context.packageManager
        if (packageInfoList.isNullOrEmpty()) {
            packageInfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
        }
        var index = 0
        val resultList = ArrayList<AppInfo>()
        for (packageInfo in packageInfoList) {
            val applicationInfo = packageInfo.applicationInfo
            if (BuildConfig.APPLICATION_ID == applicationInfo.packageName) {
                continue
            }
            val isSystemApp = Utils.isSystemApp(packageInfo)
            if (includeSystemApp && isSystemApp || includeUserApp && !isSystemApp) {
                val appName = pm.getApplicationLabel(applicationInfo).toString()
                val packageName = packageInfo.packageName
                val appIcon = pm.getApplicationIcon(applicationInfo)
                val appInfo = AppInfo(appName, packageName, appIcon)
                appInfo.enabled = applicationInfo.enabled
                resultList.add(appInfo)
                index++
            }
        }
        return resultList
    }

}
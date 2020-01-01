package xing.test.mywidget.configure

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xing.test.mywidget.BuildConfig
import xing.test.mywidget.R
import xing.test.mywidget.Utils
import xing.test.mywidget.appwidget.AppInfo
import xing.test.mywidget.appwidget.MyAppWidget
import xing.test.mywidget.configure.WidgetConfigureActivity
import java.util.*

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

    var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    var mOnClickListener = View.OnClickListener {
        val context: Context = this@WidgetConfigureActivity
        // When the button is clicked, store the string locally
        Utils.savePackageNameListPref(context, mAppWidgetId, mAdapter?.selectedPackageName)
        //            Utils.updateStateListPref(context, mAppEnableStateMap);
// It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        MyAppWidget.Companion.updateAppWidget(context, appWidgetManager, mAppWidgetId)
        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
    private var mIncludeSystemApp = false
    private var mIncludeUserApp = true
    private val mAppInfoList: MutableList<AppInfo> = ArrayList()
    private val mAppEnableStateMap = HashMap<String, Boolean>()
    private var mCbSystemApp: CheckBox? = null
    private var mCbUserApp: CheckBox? = null
    private var mRvAppList: RecyclerView? = null
    private var mAdapter: AppListAdapter? = null
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
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        initView()
        initData()
    }

    private fun updateAppList() {
        getPackageList(this, mIncludeSystemApp, mIncludeUserApp)
    }

    private fun initData() {
        updateAppList()
        mAdapter = AppListAdapter(this, mAppInfoList)
        mRvAppList!!.adapter = mAdapter
    }

    private fun initView() {
        findViews()
        findViewById<View>(R.id.add_button).setOnClickListener(mOnClickListener)
        mCbSystemApp!!.isChecked = mIncludeSystemApp
        mCbUserApp!!.isChecked = mIncludeUserApp
        mRvAppList!!.layoutManager = GridLayoutManager(this, 4)
        mCbSystemApp!!.setOnCheckedChangeListener { buttonView, isChecked ->
            mIncludeSystemApp = isChecked
            updateAppList()
            mAdapter!!.notifyDataSetChanged()
        }
        mCbUserApp!!.setOnCheckedChangeListener { buttonView, isChecked ->
            mIncludeUserApp = isChecked
            updateAppList()
            mAdapter!!.notifyDataSetChanged()
        }
    }

    private fun findViews() {
        mCbSystemApp = findViewById(R.id.cb_system)
        mCbUserApp = findViewById(R.id.cb_user)
        mRvAppList = findViewById(R.id.rv_app_list)
        findViewById<View>(R.id.btn_select_all).setOnClickListener { mAdapter!!.selectAll() }
        findViewById<View>(R.id.btn_un_select_all).setOnClickListener { mAdapter!!.unSelectAll() }
    }

    private fun getPackageList(context: Context, includeSystemApp: Boolean, includeUserApp: Boolean) {
        mAppInfoList.clear()
        val pm = context.packageManager
        val packageInfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
        var index = 0
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
                mAppInfoList.add(appInfo)
                mAppEnableStateMap[packageName] = appInfo.enabled
                index++
            }
        }
    }

}
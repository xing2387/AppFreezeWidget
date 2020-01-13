package xing.appwidget.activity

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_widget_configure.*
import kotlinx.android.synthetic.main.activity_widget_configure.app_filter
import xing.appwidget.WidgetAppList
import xing.appwidget.R
import xing.appwidget.bean.AppInfo
import xing.appwidget.bean.PackageFilterParam
import xing.appwidget.fragment.LabelManagerFragment
import xing.appwidget.storage.AddPackageListTask
import xing.appwidget.storage.AppInfoStorageHelper
import xing.appwidget.storage.LabelStorageHelper
import xing.appwidget.storage.SharedPreferenceHelper
import xing.appwidget.widget.AppFilter

class AppListWidgetConfigureActivity : AppCompatActivity(), AddPackageListTask.OnDataRequestedCallback, LabelManagerFragment.OnLabelSelectedListener {

    companion object {
        private const val TAG = "MyAppWidgetConfigureAct"
        private const val EXTRA_TEST_APPWIDGET_ID = 10086
        fun startTest(context: Context) {
            val intent = Intent(context, AppListWidgetConfigureActivity::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, EXTRA_TEST_APPWIDGET_ID)
            context.startActivity(intent)
        }
    }

    private val disposeList = ArrayList<Disposable>()

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

    override fun onDestroy() {
        doDispose()
        super.onDestroy()
    }

    private fun getData(isInit: Boolean, param: PackageFilterParam?) {
        if (param == null) {
            return
        }

        val context = this@AppListWidgetConfigureActivity
        val single1: Single<List<AppInfo>> = AppInfoStorageHelper
                .getAppInfoWithFilter(context.packageManager, param)
        val tempLabelList = HashSet<String>()
        var single2: Single<List<Set<String>>> =
                if (!isInit) Single.just(ArrayList(0))
                else tempLabelList.toFlowable()
                        .map { label -> LabelStorageHelper.getPackageNameListByLabel(label) }
                        .toList()
        val biFunction = BiFunction<List<AppInfo>, List<Set<String>>, Unit> { t1, t2 ->
            if (t2.isNotEmpty()) {
                val set = HashSet<String>()
                t2.forEach { set.addAll(it) }
                rv_app_list.setSelectedPacakgeByName(set)
            }
            rv_app_list.setData(t1)
        }
        val disposable = Single.zip(single1, single2, biFunction)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        disposeList.add(disposable)

    }

    private fun initData() {
        app_filter.setAppListView(rv_app_list)
        app_filter.setDataProvider(object : AppFilter.DataProvider {
            override fun request(param: PackageFilterParam?) {
                getData(false, param)
            }

            override fun openLabelList(param: PackageFilterParam?) {
                LabelManagerFragment.start(this@AppListWidgetConfigureActivity, false,
                        onLabelSelectedListener = this@AppListWidgetConfigureActivity,
                        selectedLabels = app_filter.getLabels())
            }
        })
        val param = PackageFilterParam(user = true, initWithGrid = true)
        getData(true, param)
        app_filter.setParam(param)
    }

    private fun initView() {
        btn_select_all.setOnClickListener { v -> rv_app_list.selectAll() }
        btn_un_select_all.setOnClickListener { rv_app_list.unSelectAll() }
        tv_btn_done.setOnClickListener {
            val context: Context = this@AppListWidgetConfigureActivity
            // When the button is clicked, store the string locally
            SharedPreferenceHelper.savePackageNameListPref(context, appWidgetId, rv_app_list.getSelectedPackageName())
            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            WidgetAppList.updateAppWidget(context, appWidgetManager, appWidgetId)
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

    override fun onSelected(labels: Collection<String>) {
        app_filter.setLabels(labels)
    }


    private fun doDispose() {
        for (disposable in disposeList) {
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
        disposeList.clear()
    }
}
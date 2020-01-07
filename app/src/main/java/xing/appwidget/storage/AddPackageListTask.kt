package xing.appwidget.storage

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.util.Log
import xing.appwidget.App
import xing.appwidget.BuildConfig
import xing.appwidget.activity.WidgetConfigureActivity
import xing.appwidget.bean.AppInfo
import xing.appwidget.bean.PackageFilterParam
import xing.appwidget.utils.Utils
import java.lang.ref.WeakReference

class AddPackageListTask : AsyncTask<PackageFilterParam, Int, List<AppInfo>> {

    companion object {
        private val TAG = AddPackageListTask::class.java.simpleName
        private val packageInfoList: MutableList<PackageInfo> = ArrayList()

        fun getCachePackageInfoList(): List<PackageInfo> = packageInfoList

        fun packageInfo2AppInfo(pm: PackageManager, packageInfo: PackageInfo): AppInfo {
            val applicationInfo = packageInfo.applicationInfo
            val appName = pm.getApplicationLabel(applicationInfo).toString()
            val packageName = packageInfo.packageName
            val appIcon = pm.getApplicationIcon(applicationInfo)
            val appInfo = AppInfo(appName, packageName, appIcon, applicationInfo.enabled)
            return appInfo
        }
    }

    private var callback: WeakReference<OnDataRequestedCallback>? = null

    constructor(callback: OnDataRequestedCallback) : super() {
        this.callback = WeakReference(callback)
    }

    override fun doInBackground(vararg params: PackageFilterParam): List<AppInfo> {
        val param = params[0]
        return getPackageList(App.app, param.system, param.user)
    }

    override fun onProgressUpdate(vararg values: Int?) {
    }

    override fun onPostExecute(result: List<AppInfo>) {
        callback?.get()?.onAppListGet(result)
    }

    private fun getPackageList(context: Context?, includeSystemApp: Boolean, includeUserApp: Boolean): List<AppInfo> {
        if (context == null) {
            return ArrayList(0)
        }
        val pm = context.packageManager
        if (packageInfoList.isEmpty()) {
            packageInfoList.addAll(pm.getInstalledPackages(PackageManager.GET_ACTIVITIES))
        }
        var index = 0
        val resultList = ArrayList<AppInfo>()
        for (packageInfo in packageInfoList) {
            if (BuildConfig.APPLICATION_ID == packageInfo.applicationInfo.packageName) {
                continue
            }
            val isSystemApp = Utils.isSystemApp(packageInfo)
            if (includeSystemApp && isSystemApp || includeUserApp && !isSystemApp) {
                resultList.add(packageInfo2AppInfo(pm, packageInfo))
                index++
            }
        }
        return resultList
    }


    interface OnDataRequestedCallback {
        fun onAppListGet(result: List<AppInfo>)
    }

}
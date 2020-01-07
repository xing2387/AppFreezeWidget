package xing.appwidget.storage

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xing.appwidget.BuildConfig
import xing.appwidget.bean.AppInfo
import xing.appwidget.bean.PackageFilterParam

class AppInfoStorageHelper {

    companion object {
        private val TAG = AppInfoStorageHelper::class.java.simpleName

        private val packageInfoList: MutableList<PackageInfo> = ArrayList()

        fun getAppInfoWithFilter(pm: PackageManager, filter: PackageFilterParam): Single<List<AppInfo>> {
            return Single.just(filter)
                    .map { f -> getAppInfoList(pm, f) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }

        fun packageInfo2AppInfo(pm: PackageManager, packageInfo: PackageInfo): AppInfo {
            val applicationInfo = packageInfo.applicationInfo
            val appName = pm.getApplicationLabel(applicationInfo).toString()
            val packageName = packageInfo.packageName
            val appIcon = pm.getApplicationIcon(applicationInfo)
            val appInfo = AppInfo(appName, packageName, appIcon, applicationInfo.enabled)
            return appInfo
        }

        fun getAppInfoList(pm: PackageManager, filter: PackageFilterParam): List<AppInfo> {
            if (packageInfoList.isEmpty()) {
                packageInfoList.addAll(pm.getInstalledPackages(PackageManager.GET_ACTIVITIES))
            }
            val resultList = ArrayList<AppInfo>()
            for (packageInfo in packageInfoList) {
                if (BuildConfig.APPLICATION_ID == packageInfo.applicationInfo.packageName) {
                    continue
                }
                if (filter.isValid(packageInfo)) {
                    resultList.add(packageInfo2AppInfo(pm, packageInfo))
                }
            }
            return resultList
        }
    }
}
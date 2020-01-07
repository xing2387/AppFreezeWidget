package xing.appwidget.bean

import android.content.pm.PackageInfo
import xing.appwidget.storage.LabelStorageHelper
import xing.appwidget.utils.Utils
import java.util.*

data class PackageFilterParam(var system: Boolean = false, var user: Boolean = false,
                              var enabled: Boolean = true, var disabled: Boolean = false,
                              var labels: Collection<String> = Collections.emptyList(), var initWithGrid: Boolean) {

    fun isValid(packageInfo: PackageInfo): Boolean {

        val isSystemApp = Utils.isSystemApp(packageInfo)
        val appEnabled = packageInfo.applicationInfo.enabled
        val packageName = packageInfo.packageName

        val systemAppFilter = system and isSystemApp || user and !isSystemApp
        val enabledAppFilter = enabled and appEnabled || disabled and !appEnabled
        var inLabels = true
        for (label in labels) {
            inLabels = LabelStorageHelper.getPackageNameListByLabel(label).contains(packageName)
            if (inLabels) {
                break
            }
        }

        return systemAppFilter && enabledAppFilter && inLabels
    }
}
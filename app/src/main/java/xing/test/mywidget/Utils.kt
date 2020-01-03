package xing.test.mywidget

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import java.util.*

@SuppressLint("ApplySharedPref")
object Utils {
    const val PREFS_MAIN = "xing.test.mywidget.appwidget.MyAppWidget"
    const val PREFS_LABELS = "labels"
    const val PREF_PREFIX_KEY_PACKAGE_NAME = "appwidget_packagenames_"
    const val PREF_PREFIX_KEY_EDIT_MODE = "appwidget_edit_mode_"

    fun updateStateListPref(context: Context, enableStateMap: HashMap<String?, Boolean?>) {
        val prefs = context.getSharedPreferences(PREFS_MAIN, 0).edit()
        for (packageName in enableStateMap.keys) {
            prefs.putBoolean(packageName, enableStateMap[packageName]!!)
        }
        prefs.commit()
    }

    fun getMainPref(context: Context) = context.getSharedPreferences(PREFS_MAIN, Context.MODE_PRIVATE)

    fun getEnableState(context: Context, packageName: String?) =
            getMainPref(context).getBoolean(packageName, true)

    fun saveEnableState(context: Context, packageName: String?, isEnabled: Boolean) =
            getMainPref(context).edit().putBoolean(packageName, isEnabled).commit()

    /**
     * 保存桌面部件显示的app列表
     */
    fun savePackageNameListPref(context: Context, appWidgetId: Int, packageNameList: Set<String?>?) =
            getMainPref(context).edit().putStringSet(PREF_PREFIX_KEY_PACKAGE_NAME + appWidgetId, packageNameList).commit()

    fun loadPackageNameListPref(context: Context, appWidgetId: Int) =
            getMainPref(context).getStringSet(PREF_PREFIX_KEY_PACKAGE_NAME + appWidgetId, Collections.emptySet())

    fun deletePackageNameListPref(context: Context, appWidgetId: Int) =
            getMainPref(context).edit().remove(PREF_PREFIX_KEY_PACKAGE_NAME + appWidgetId).apply()

    fun setEditModePref(context: Context, appWidgetId: Int, isEditMode: Boolean) =
            getMainPref(context).edit().putBoolean(PREF_PREFIX_KEY_EDIT_MODE + appWidgetId, isEditMode).commit()

    fun loadEditModePref(context: Context, appWidgetId: Int) =
            getMainPref(context).getBoolean(PREF_PREFIX_KEY_EDIT_MODE + appWidgetId, true)

    fun getLabelPref(context: Context) = context.getSharedPreferences(PREFS_LABELS, Context.MODE_PRIVATE)

    fun saveLabelPref(context: Context, label: String, packageNameSet: Set<String>) =
            getLabelPref(context).edit().putStringSet(label, packageNameSet).commit()

    fun getLabelContent(context: Context, label: String) =
            getLabelPref(context).getStringSet(label, Collections.emptySet())

    fun isSystemApp(pInfo: PackageInfo): Boolean { //判断是否是系统软件
        return pInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    fun isSystemUpdateApp(pInfo: PackageInfo): Boolean { //判断是否是软件更新..
        return pInfo.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
    }

    fun isUserApp(pInfo: PackageInfo): Boolean { //是否是系统软件或者是系统软件更新
        return !isSystemApp(pInfo) && !isSystemUpdateApp(pInfo)
    }
}
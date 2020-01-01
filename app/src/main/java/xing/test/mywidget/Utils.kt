package xing.test.mywidget

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import java.util.*

object Utils {
    const val PREFS_NAME = "xing.test.mywidget.appwidget.MyAppWidget"
    const val PREF_PREFIX_KEY_PACKAGE_NAME = "appwidget_packagenames_"
    const val PREF_PREFIX_KEY_EDIT_MODE = "appwidget_edit_mode_"
    fun updateStateListPref(context: Context, enableStateMap: HashMap<String?, Boolean?>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
        for (packageName in enableStateMap.keys) {
            prefs.putBoolean(packageName, enableStateMap[packageName]!!)
        }
        prefs.commit()
    }

    fun getEnableState(context: Context, packageName: String?): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getBoolean(packageName, true)
    }

    fun saveEnableState(context: Context, packageName: String?, isEnabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
        prefs.putBoolean(packageName, isEnabled)
        prefs.commit()
    }

    fun savePackageNameListPref(context: Context, appWidgetId: Int, packageNameList: HashSet<String?>?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
        prefs.putStringSet(PREF_PREFIX_KEY_PACKAGE_NAME + appWidgetId, packageNameList)
        prefs.commit()
    }

    fun loadPackageNameListPref(context: Context, appWidgetId: Int): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        val set = prefs.getStringSet(PREF_PREFIX_KEY_PACKAGE_NAME + appWidgetId, null)
        return set ?: HashSet()
    }

    fun deletePackageNameListPref(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
        prefs.remove(PREF_PREFIX_KEY_PACKAGE_NAME + appWidgetId)
        prefs.apply()
    }

    fun setEditModePref(context: Context, appWidgetId: Int, isEditMode: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
        prefs.putBoolean(PREF_PREFIX_KEY_EDIT_MODE + appWidgetId, isEditMode)
        prefs.commit()
    }

    fun loadEditModePref(context: Context, appWidgetId: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getBoolean(PREF_PREFIX_KEY_EDIT_MODE + appWidgetId, true)
    }

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
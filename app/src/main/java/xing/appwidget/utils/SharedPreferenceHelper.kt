package xing.appwidget.utils

import android.annotation.SuppressLint
import android.content.Context
import java.util.*

@SuppressLint("ApplySharedPref")
object SharedPreferenceHelper {
    const val PREFS_MAIN = "xing.appwidget.MyAppWidget"
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

    /*-----------   标签相关  start -------------*/
    fun getLabelPref(context: Context) = context.getSharedPreferences(PREFS_LABELS, Context.MODE_PRIVATE)

    fun saveLabelPref(context: Context, label: String, packageNameSet: Set<String>) =
            getLabelPref(context).edit().putStringSet(label, packageNameSet).commit()

    fun getLabelContent(context: Context, label: String) =
            getLabelPref(context).getStringSet(label, Collections.emptySet())
    /*-----------   标签相关  end -------------*/

}
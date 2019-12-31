package xing.test.mywidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Utils {


    public static final String PREFS_NAME = "xing.test.mywidget.appwidget.MyAppWidget";
    public static final String PREF_PREFIX_KEY_PACKAGE_NAME = "appwidget_packagenames_";
    public static final String PREF_PREFIX_KEY_EDIT_MODE = "appwidget_edit_mode_";

    public static void updateStateListPref(Context context, @NonNull HashMap<String, Boolean> enableStateMap) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        for (String packageName : enableStateMap.keySet()) {
            prefs.putBoolean(packageName, enableStateMap.get(packageName));
        }
        prefs.commit();
    }

    public static boolean getEnableState(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(packageName, true);
    }

    public static void saveEnableState(Context context, String packageName, boolean isEnabled) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putBoolean(packageName, isEnabled);
        prefs.commit();
    }


    public static void savePackageNameListPref(Context context, int appWidgetId, HashSet<String> packageNameList) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putStringSet(PREF_PREFIX_KEY_PACKAGE_NAME + appWidgetId, packageNameList);
        prefs.commit();
    }

    public static Set<String> loadPackageNameListPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Set<String> set = prefs.getStringSet(PREF_PREFIX_KEY_PACKAGE_NAME + appWidgetId, null);
        if (set != null) {
            return set;
        } else {
            return new HashSet<>();
        }
    }

    public static void deletePackageNameListPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY_PACKAGE_NAME + appWidgetId);
        prefs.apply();
    }

    public static void setEditModePref(Context context, int appWidgetId, boolean isEditMode) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putBoolean(PREF_PREFIX_KEY_EDIT_MODE + appWidgetId, isEditMode);
        prefs.commit();
    }

    public static boolean loadEditModePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(PREF_PREFIX_KEY_EDIT_MODE + appWidgetId, true);
    }

    public static boolean isSystemApp(PackageInfo pInfo) {
        //判断是否是系统软件
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public static boolean isSystemUpdateApp(PackageInfo pInfo) {
        //判断是否是软件更新..
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public static boolean isUserApp(PackageInfo pInfo) {
        //是否是系统软件或者是系统软件更新
        return (!isSystemApp(pInfo) && !isSystemUpdateApp(pInfo));
    }
}

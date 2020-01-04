package xing.appwidget.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo

class Utils {

    companion object {
        fun isSystemApp(pInfo: PackageInfo): Boolean { //判断是否是系统软件（系统软件更新后也成为了User）
            return pInfo.applicationInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        }
    }
}
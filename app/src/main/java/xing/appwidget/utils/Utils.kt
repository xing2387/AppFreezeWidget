package xing.appwidget.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo

class Utils {

    companion object {
        fun isSystemApp(pInfo: PackageInfo): Boolean { //判断是否是系统软件（系统软件更新后也成为了User）
            return pInfo.applicationInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        }

        fun scanForActivity(context: Context?): Activity? {
            return if (context == null) {
                null
            } else if (context is Activity) {
                context
            } else {
                if (context is ContextWrapper) scanForActivity(context.baseContext) else null
            }
        }

        fun tryConvertToActivity(context: Context): Context {
            return (scanForActivity(context) ?: context)
        }
    }
}
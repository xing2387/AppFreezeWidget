package xing.appwidget.bean

import android.graphics.drawable.Drawable

data class AppInfo(var appName: String, var packageName: String, var appIcon: Drawable, var enabled: Boolean) {

    override fun toString(): String {
        return "appName = " + appName + "," +
                "packageName = " + packageName + "," +
                "enable = " + enabled
    }

}
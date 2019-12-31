package xing.test.mywidget.appwidget;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public AppInfo(String appName, String packageName, Drawable appIcon) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
    }

    public String appName;
    public String packageName;
    public Drawable appIcon;
    public boolean enabled;


    @Override
    public String toString() {
        return "appName = " + appName + "," +
                "packageName = " + packageName + "," +
                "enable = " + enabled;
    }
}

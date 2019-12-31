
package xing.test.mywidget.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xing.test.mywidget.R;
import xing.test.mywidget.Utils;

public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new xing.test.mywidget.appwidget.StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "StackRemoteViewsFactory";

    private Context mContext;
    private int mAppWidgetId;
    private final int mIconSize;
    private boolean mIsEditMode;

    private HashSet<String> mPackageNameList = new HashSet<>();
    private ArrayList<AppInfo> mAppInfoList = new ArrayList<>();

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        mIconSize = mContext.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        mAppInfoList.clear();


        Set<String> packages = Utils.loadPackageNameListPref(mContext, mAppWidgetId);
        Log.d(TAG, "onCreate: " + mAppWidgetId + "," + packages);
        PackageManager pm = mContext.getPackageManager();
        List<ApplicationInfo> applicationInfoList = pm.getInstalledApplications(0);
        for (ApplicationInfo info : applicationInfoList) {
            if (packages.contains(info.packageName)) {
                String appName = pm.getApplicationLabel(info).toString();
                String packageName = info.packageName;
                Drawable appIcon = pm.getApplicationIcon(info);
                AppInfo appInfo = new AppInfo(appName, packageName, appIcon);
                appInfo.enabled = info.enabled;
                Log.d(TAG, "onCreate: " + appName + " --> " + appInfo.enabled);
                mAppInfoList.add(appInfo);
            }
        }
        Log.d(TAG, "onCreate: " + mAppInfoList);

        // We sleep for 3 seconds here to show how the empty view appears in the interim.
        // The empty view is set in the StackWidgetProvider and should be a sibling of the
        // collection view.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
//        mWidgetItems.clear();
    }

    public int getCount() {
        Log.d(TAG, "getCount: " + mAppInfoList.size());
        return mAppInfoList.size();
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        AppInfo appInfo = mAppInfoList.get(position);
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_app);
        rv.setTextViewText(R.id.tv_name, appInfo.appName);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                appInfo.appIcon instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable drawable = (AdaptiveIconDrawable) appInfo.appIcon;

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

//            Bitmap shadow = getShadowBitmap(drawable);
            appInfo.appIcon = new BitmapDrawable(mContext.getResources(), bitmap);
        }
        if (appInfo.appIcon instanceof BitmapDrawable) {
            rv.setImageViewBitmap(R.id.iv_icon, ((BitmapDrawable) appInfo.appIcon).getBitmap());
        } else {
            Log.w(TAG, "getViewAt: icon drawable is " + appInfo.appIcon.getClass().getSimpleName());
        }
        rv.setViewVisibility(R.id.v_mask, appInfo.enabled ? View.GONE : View.VISIBLE);


        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putString(MyAppWidget.APP_LIST, appInfo.packageName);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.layout_item, fillInIntent);

        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
        Log.d(TAG, "getViewAt: " + appInfo.appName + " --> " + appInfo.enabled);
        // Return the remote views object.
        return rv;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
        mIsEditMode = Utils.loadEditModePref(mContext, mAppWidgetId);
        for (AppInfo info : mAppInfoList) {
            info.enabled = Utils.getEnableState(mContext, info.packageName);
        }
    }

}


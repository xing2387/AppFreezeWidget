package xing.appwidget.service

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.RemoteViewsService.RemoteViewsFactory
import xing.appwidget.MyAppWidget
import xing.appwidget.R
import xing.appwidget.bean.AppInfo
import xing.appwidget.utils.SharedPreferenceHelper
import java.util.*

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return StackRemoteViewsFactory(this.applicationContext, intent)
    }
}

internal class StackRemoteViewsFactory(private val mContext: Context, intent: Intent) : RemoteViewsFactory {
    companion object {
        private const val TAG = "StackRemoteViewsFactory"
    }

    private val mAppWidgetId: Int
    private val mIconSize: Int
    private var mIsEditMode = false
    private val mPackageNameList = HashSet<String>()
    private val mAppInfoList = ArrayList<AppInfo>()

    init {
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
        mIconSize = mContext.resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
    }

    override fun onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        mAppInfoList.clear()
        val packages = SharedPreferenceHelper.loadPackageNameListPref(mContext, mAppWidgetId)
        Log.d(TAG, "onCreate: $mAppWidgetId,$packages")
        val pm = mContext.packageManager
        val applicationInfoList = pm.getInstalledApplications(0)
        for (info in applicationInfoList) {
            if (packages!!.contains(info.packageName)) {
                val appName = pm.getApplicationLabel(info).toString()
                val packageName = info.packageName
                val appIcon = pm.getApplicationIcon(info)
                val appInfo = AppInfo(appName, packageName, appIcon)
                appInfo.enabled = info.enabled
                Log.d(TAG, "onCreate: " + appName + " --> " + appInfo.enabled)
                mAppInfoList.add(appInfo)
            }
        }
        Log.d(TAG, "onCreate: $mAppInfoList")
        // We sleep for 3 seconds here to show how the empty view appears in the interim.
        // The empty view is set in the StackWidgetProvider and should be a sibling of the
        // collection view.
        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        //        mWidgetItems.clear();
    }

    override fun getCount(): Int {
        Log.d(TAG, "getCount: " + mAppInfoList.size)
        return mAppInfoList.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        // position will always range from 0 to getCount() - 1.
        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        val appInfo = mAppInfoList[position]
        val rv = RemoteViews(mContext.packageName, R.layout.item_widget_app)
        rv.setTextViewText(R.id.tv_name, appInfo.appName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                appInfo.appIcon is AdaptiveIconDrawable) {
            val drawable = appInfo.appIcon as AdaptiveIconDrawable
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            //            Bitmap shadow = getShadowBitmap(drawable);
            appInfo.appIcon = BitmapDrawable(mContext.resources, bitmap)
        }
        if (appInfo.appIcon is BitmapDrawable) {
            rv.setImageViewBitmap(R.id.iv_icon, (appInfo.appIcon as BitmapDrawable).bitmap)
        } else {
            Log.w(TAG, "getViewAt: icon drawable is " + appInfo.appIcon.javaClass.simpleName)
        }
        rv.setViewVisibility(R.id.v_mask, if (appInfo.enabled) View.GONE else View.VISIBLE)
        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        val extras = Bundle()
        extras.putString(MyAppWidget.APP_LIST, appInfo.packageName)
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)
        rv.setOnClickFillInIntent(R.id.layout_item, fillInIntent)
        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
        Log.d(TAG, "getViewAt: " + appInfo.appName + " --> " + appInfo.enabled)
        // Return the remote views object.
        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
        mIsEditMode = SharedPreferenceHelper.loadEditModePref(mContext, mAppWidgetId)
        for (info in mAppInfoList) {
            info.enabled = SharedPreferenceHelper.getEnableState(mContext, info.packageName)
        }
    }

}
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
import xing.appwidget.R
import xing.appwidget.WidgetBase
import xing.appwidget.bean.AppInfo
import xing.appwidget.storage.SharedPreferenceHelper
import java.util.*
import kotlin.collections.ArrayList

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
    private var mAppInfoList: ArrayList<AppInfo>? = null

    init {
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
        mIconSize = mContext.resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
    }

    override fun onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        mAppInfoList?.clear()
        val packages = SharedPreferenceHelper.loadPackageNameListPref(mContext, mAppWidgetId)
        if (!packages.isNullOrEmpty()) {
            mPackageNameList.addAll(packages)
        }
        Log.d(TAG, "onCreate: $mAppInfoList")
    }

    override fun onDestroy() {
        mPackageNameList.clear()
        mAppInfoList?.clear()
        mAppInfoList = null
    }

    override fun getCount(): Int {
        Log.d(TAG, "getCount: " + mPackageNameList.size)
        return mPackageNameList.size
    }

    override fun getViewAt(position: Int): RemoteViews {

        if (mAppInfoList == null) {
            synchronized(this) {
                if (mAppInfoList == null) {
                    mAppInfoList = ArrayList(mPackageNameList.size)
                    val pm = mContext.packageManager
                    val applicationInfoList = pm.getInstalledApplications(0)
                    for (info in applicationInfoList) {
                        if (mPackageNameList.contains(info.packageName)) {
                            val appName = pm.getApplicationLabel(info).toString()
                            val packageName = info.packageName
                            val appIcon = pm.getApplicationIcon(info)
                            val appInfo = AppInfo(appName, packageName, appIcon, info.enabled)
                            SharedPreferenceHelper.saveEnableState(mContext, packageName, appInfo.enabled)

                            Log.d(TAG, "onCreate: " + appName + " --> " + appInfo.enabled)
                            mAppInfoList?.add(appInfo)
                        }
                    }
                }
            }
        }

        // position will always range from 0 to getCount() - 1.
        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        val appInfo = mAppInfoList?.get(position)
        val remoteView = RemoteViews(mContext.packageName, R.layout.item_widget_app)
        if (appInfo == null) {
            return remoteView
        }
        remoteView.setTextViewText(R.id.tv_name, appInfo.appName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo.appIcon is AdaptiveIconDrawable) {
            val drawable = appInfo.appIcon as AdaptiveIconDrawable
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            appInfo.appIcon = BitmapDrawable(mContext.resources, bitmap)
        }
        if (appInfo.appIcon is BitmapDrawable) {
            remoteView.setImageViewBitmap(R.id.iv_icon, (appInfo.appIcon as BitmapDrawable).bitmap)
        } else {
            Log.w(TAG, "getViewAt: icon drawable is " + appInfo.appIcon.javaClass.simpleName)
        }
        remoteView.setViewVisibility(R.id.v_mask, if (appInfo.enabled) View.GONE else View.VISIBLE)
        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        val extras = Bundle()
        extras.putString(WidgetBase.APP_LIST, appInfo.packageName)
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)
        remoteView.setOnClickFillInIntent(R.id.layout_item, fillInIntent)
        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
        Log.d(TAG, "getViewAt: " + appInfo.appName + " --> " + appInfo.enabled)
        // Return the remote views object.
        return remoteView
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
        val appInfoList = mAppInfoList
        if (appInfoList != null) {
            for (info in appInfoList) {
                info.enabled = SharedPreferenceHelper.getEnableState(mContext, info.packageName)
            }
        }
    }

}
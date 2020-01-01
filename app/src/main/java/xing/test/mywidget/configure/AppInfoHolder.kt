package xing.test.mywidget.configure

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import xing.test.mywidget.R
import xing.test.mywidget.appwidget.AppInfo

internal class AppInfoHolder(itemView: View) : ViewHolder(itemView) {
    private val mIvAppIcon: ImageView
    private val mTvAppName: TextView
    private val mCbCheck: CheckBox
    private var mOnItemCheckedListener: OnItemCheckedListener? = null
    private var mAppInfo: AppInfo? = null
    fun bindView(appInfo: AppInfo, isChecked: Boolean) {
        mAppInfo = appInfo
        mIvAppIcon.setImageDrawable(appInfo.appIcon)
        mTvAppName.text = appInfo.appName
        mCbCheck.isChecked = isChecked
    }

    fun setOnItemCheckedListener(listener: OnItemCheckedListener?) {
        mOnItemCheckedListener = listener
    }

    internal interface OnItemCheckedListener {
        fun onCheckedChanged(isChecked: Boolean, packageName: String?)
    }

    init {
        mIvAppIcon = itemView.findViewById(R.id.iv_icon)
        mTvAppName = itemView.findViewById(R.id.tv_name)
        mCbCheck = itemView.findViewById(R.id.cb_check)
        mCbCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (mOnItemCheckedListener != null) {
                mOnItemCheckedListener!!.onCheckedChanged(isChecked, mAppInfo!!.packageName)
            }
        }
    }
}
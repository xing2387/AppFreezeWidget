package xing.test.mywidget.configure

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import xing.test.mywidget.R
import xing.test.mywidget.appwidget.AppInfo
import xing.test.mywidget.configure.AppInfoHolder.OnItemCheckedListener
import java.util.*

internal class AppListAdapter(private val mContext: Context, private val mAppInfoList: List<AppInfo>?) :
        RecyclerView.Adapter<ViewHolder>(), OnItemCheckedListener {
    val selectedPackageName = HashSet<String?>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_configure_app, viewGroup, false)
        val holder = AppInfoHolder(view)
        holder.setOnItemCheckedListener(this)
        return holder
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        if (viewHolder is AppInfoHolder && mAppInfoList!!.size > i) {
            val appInfo = mAppInfoList[i]
            val isChecked = selectedPackageName.contains(appInfo.packageName)
            viewHolder.bindView(appInfo, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return mAppInfoList?.size ?: 0
    }

    override fun onCheckedChanged(isChecked: Boolean, packageName: String?) {
        if (isChecked) {
            selectedPackageName.add(packageName)
        } else {
            selectedPackageName.remove(packageName)
        }
    }

    fun selectAll() {
        for (appInfo in mAppInfoList!!) {
            selectedPackageName.add(appInfo.packageName)
        }
        notifyDataSetChanged()
    }

    fun unSelectAll() {
        selectedPackageName.clear()
        notifyDataSetChanged()
    }

}
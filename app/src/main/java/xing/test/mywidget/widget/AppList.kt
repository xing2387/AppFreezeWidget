package xing.test.mywidget.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_configure_app.view.*
import kotlinx.android.synthetic.main.item_widget_app.view.iv_icon
import kotlinx.android.synthetic.main.item_widget_app.view.tv_name
import xing.test.mywidget.R
import xing.test.mywidget.appwidget.AppInfo

class AppList(context: Context, attrs: AttributeSet? = null) : RecyclerView(context, attrs) {

    private val adapter: AppListAdapter

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = AppListAdapter(context)
        setAdapter(adapter)
    }

    fun setData(appInfoList: List<AppInfo>?) {
        adapter.setData(appInfoList)
    }

    fun getSelectedPackageName(): Set<String?> {
        return adapter.selectedPackageName
    }

    fun selectAll() {
        adapter.selectAll()
    }

    fun unSelectAll() {
        adapter.unSelectAll()
    }
}

internal class AppListAdapter(private val context: Context) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), AppInfoHolder.OnItemCheckedListener {

    private val appInfoList: MutableList<AppInfo> = ArrayList()
    val selectedPackageName = HashSet<String?>()

    fun setData(appInfoList: List<AppInfo>?) {
        this.appInfoList.clear()
        if (!appInfoList.isNullOrEmpty()) {
            this.appInfoList.addAll(appInfoList)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_configure_app, viewGroup, false)
        val holder = AppInfoHolder(view)
        holder.onItemCheckedListener = this
        return holder
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        if (viewHolder is AppInfoHolder && appInfoList.size > i) {
            val appInfo = appInfoList[i]
            val isChecked = selectedPackageName.contains(appInfo.packageName)
            viewHolder.bindView(appInfo, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return appInfoList.size
    }

    override fun onCheckedChanged(isChecked: Boolean, packageName: String?) {
        if (isChecked) {
            selectedPackageName.add(packageName)
        } else {
            selectedPackageName.remove(packageName)
        }
    }

    fun selectAll() {
        if (appInfoList.isNullOrEmpty()) {
            return
        }
        for (appInfo in appInfoList) {
            selectedPackageName.add(appInfo.packageName)
        }
        notifyDataSetChanged()
    }

    fun unSelectAll() {
        selectedPackageName.clear()
        notifyDataSetChanged()
    }

}

internal class AppInfoHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var appInfo: AppInfo? = null
    var onItemCheckedListener: OnItemCheckedListener? = null

    fun bindView(appInfo: AppInfo?, isChecked: Boolean) {
        this.appInfo = appInfo
        if (appInfo == null) {
            return
        }
        itemView.iv_icon.setImageDrawable(appInfo.appIcon)
        itemView.tv_name.text = appInfo.appName
        itemView.cb_check.isChecked = isChecked
        itemView.setOnClickListener(this)
    }

    internal interface OnItemCheckedListener {
        fun onCheckedChanged(isChecked: Boolean, packageName: String?)
    }

    override fun onClick(v: View?) {
        itemView.cb_check.isChecked = !itemView.cb_check.isChecked
    }

    init {
        itemView.cb_check.setOnCheckedChangeListener { buttonView, isChecked ->
            onItemCheckedListener?.onCheckedChanged(isChecked, appInfo?.packageName)
        }
    }
}
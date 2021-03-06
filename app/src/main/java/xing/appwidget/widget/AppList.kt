package xing.appwidget.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_configure_app_linear.view.*
import xing.appwidget.R
import xing.appwidget.bean.AppInfo

class AppList : RecyclerView {

    private val adapter: AppListAdapter
    private var isGrid = true

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AppList)
        isGrid = ta.getBoolean(R.styleable.AppList_al_isGrid, isGrid)
        ta.recycle()

        adapter = AppListAdapter(context, isGrid)
        if (isGrid) showInGrids() else showInRows()
        setAdapter(adapter)
    }

    fun showInGrids() {
        layoutManager = GridLayoutManager(context, 4)
        adapter.setStyle(true)
    }

    fun showInRows() {
        layoutManager = LinearLayoutManager(context)
        adapter.setStyle(false)
    }

    fun setData(appInfoList: Collection<AppInfo>?) {
        adapter.setData(appInfoList)
    }

    fun getSelectedPackageName(): Set<String> {
        return adapter.selectedPackageName
    }

    fun setSelectedPacakgeByName(packageNameList: Collection<String>) {
        adapter.selectedPackageName.clear()
        adapter.selectedPackageName.addAll(packageNameList)
        adapter.notifyDataSetChanged()
    }

    fun selectAll() {
        adapter.selectAll()
    }

    fun unSelectAll() {
        adapter.unSelectAll()
    }

    fun setEdieMode(enable: Boolean) {
        adapter.setEditMode(enable)
    }
}

internal class AppListAdapter(private val context: Context, private var isGrid: Boolean = true) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), AppInfoHolder.OnItemCheckedListener {

    private val appInfoList: MutableList<AppInfo> = ArrayList()
    val selectedPackageName = HashSet<String>()
    private var editMode: Boolean = true

    fun setStyle(isGrid: Boolean) {
        this.isGrid = isGrid
        notifyDataSetChanged()
    }

    fun setData(appInfoList: Collection<AppInfo>?) {

        this.appInfoList.clear()
        if (!appInfoList.isNullOrEmpty()) {
            this.appInfoList.addAll(appInfoList)
        }
        notifyDataSetChanged()
    }

    fun setEditMode(editMode: Boolean) {
        if (this.editMode == editMode) {
            return
        }
        this.editMode = editMode
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        val holder = AppInfoHolder(context, viewGroup, isGrid)
        holder.onItemCheckedListener = this
        return holder
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGrid) AppInfoHolder.VIEW_TYPE_GRID else AppInfoHolder.VIEW_TYPE_ROW
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        if (viewHolder is AppInfoHolder && appInfoList.size > i) {
            val appInfo = appInfoList[i]
            val isChecked = selectedPackageName.contains(appInfo.packageName)
            viewHolder.setEditMode(editMode)
            viewHolder.bindView(appInfo, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return appInfoList.size
    }

    override fun onCheckedChanged(isChecked: Boolean, packageName: String?) {
        if (packageName.isNullOrBlank()) {
            return
        }
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

internal class AppInfoHolder(context: Context, parent: ViewGroup?, isGrid: Boolean = false) :
        RecyclerView.ViewHolder(createItemView(context, parent, isGrid)), View.OnClickListener {

    companion object {
        const val VIEW_TYPE_GRID = 1
        const val VIEW_TYPE_ROW = 2

        fun createItemView(context: Context, parent: ViewGroup?, isGrid: Boolean): View {
            val layoutResId = if (isGrid) R.layout.item_configure_app_grid else R.layout.item_configure_app_linear
            return LayoutInflater.from(context).inflate(layoutResId, parent, false)
        }
    }

    private var editMode: Boolean = true
    private var appInfo: AppInfo? = null
    var onItemCheckedListener: OnItemCheckedListener? = null

    init {
        itemView.cb_check.setOnCheckedChangeListener { buttonView, isChecked ->
            if (editMode) {
                onItemCheckedListener?.onCheckedChanged(isChecked, appInfo?.packageName)
            }
        }
    }

    fun setEditMode(editMode: Boolean) {
        if (this.editMode == editMode) {
            return
        }
        if (editMode) {
            itemView.cb_check.visibility = View.VISIBLE
        } else {
            itemView.cb_check.visibility = View.GONE
        }
    }

    fun bindView(appInfo: AppInfo?, isChecked: Boolean) {
        this.appInfo = appInfo
        if (appInfo == null) {
            return
        }
        with(itemView, {
            iv_icon.setImageDrawable(appInfo.appIcon)
            tv_name.text = appInfo.appName
            cb_check.isChecked = isChecked
            tv_package_name?.text = appInfo.packageName
            val colorResId = if (appInfo.enabled) R.color.bg_enabled else R.color.bg_disabled
            setBackgroundColor(context.resources.getColor(colorResId))
        })
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (!editMode) {
            return
        }
        itemView.cb_check.isChecked = !itemView.cb_check.isChecked
    }

    internal interface OnItemCheckedListener {
        fun onCheckedChanged(isChecked: Boolean, packageName: String?)
    }
}
package xing.appwidget.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.merge_app_filter.view.*
import xing.appwidget.R
import xing.appwidget.bean.PackageFilterParam
import java.util.*


class AppFilter : RelativeLayout {

    private var appListView: AppList? = null
    private var dataProvider: DataProvider? = null
    private var filterParam: PackageFilterParam? = null
    private var labels: Collection<String> = Collections.emptyList()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.merge_app_filter, this, true)
    }

    fun setAppListView(appList: AppList) {
        this.appListView = appList
        checkGrid()
    }

    fun checkGrid() {
        val filterParam = filterParam
        val appListView = appListView
        if (appListView != null && filterParam != null) {
            if (filterParam.initWithGrid) appListView.showInGrids() else appListView.showInRows()
        }
    }

    fun getLabels() = labels
    fun setLabels(labels: Collection<String>) {
        this.labels = labels
        if (checkLabelSetting()) {
            dataProvider?.request(filterParam)
        }
    }

    fun setParam(initParam: PackageFilterParam) {
        this.labels = initParam.labels
        this.filterParam = initParam
        cb_user.isChecked = initParam.user
        cb_system.isChecked = initParam.system
        cb_disabled.isChecked = initParam.disabled
        cb_enabled.isChecked = initParam.enabled
        cb_grid.isChecked = initParam.initWithGrid
        checkGrid()

        cb_system.setOnCheckedChangeListener { buttonView, isChecked ->
            this.filterParam?.system = isChecked
            checkLabelSetting()
            dataProvider?.request(this.filterParam)
        }
        cb_user.setOnCheckedChangeListener { buttonView, isChecked ->
            this.filterParam?.user = isChecked
            checkLabelSetting()
            dataProvider?.request(this.filterParam)
        }
        cb_grid.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) appListView?.showInGrids() else appListView?.showInRows()
        }
        cb_disabled.setOnCheckedChangeListener { buttonView, isChecked ->
            this.filterParam?.disabled = isChecked
            checkLabelSetting()
            dataProvider?.request(this.filterParam)
        }
        cb_enabled.setOnCheckedChangeListener { buttonView, isChecked ->
            this.filterParam?.enabled = isChecked
            checkLabelSetting()
            dataProvider?.request(this.filterParam)
        }
        cb_label.setOnCheckedChangeListener { buttonView, isChecked ->
            this.filterParam?.labels = if (isChecked) labels else Collections.emptyList()
            dataProvider?.request(this.filterParam)
        }
        cb_label.setOnLongClickListener {
            dataProvider?.openLabelList(this.filterParam)
            true
        }

//        dataProvider?.request(initParam)
    }

    fun checkLabelSetting(): Boolean {
        return if (cb_label.isChecked) {
            filterParam?.labels = labels
            true
        } else {
            filterParam?.labels = Collections.emptyList()
            false
        }
    }

    fun setDataProvider(dataProvider: DataProvider?) {
        this.dataProvider = dataProvider
    }

    interface DataProvider {
        fun request(param: PackageFilterParam?)
        fun openLabelList(param: PackageFilterParam?)
    }
}
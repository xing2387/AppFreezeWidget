package xing.appwidget.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.merge_app_filter.view.*
import xing.appwidget.R
import xing.appwidget.bean.PackageFilterParam


class AppFilter : RelativeLayout {

    private var appListView: AppList? = null
    private var dataProvider: DataProvider? = null
    private var filterParam: PackageFilterParam? = null

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

    fun setParam(filterParam: PackageFilterParam) {
        this.filterParam = filterParam
        cb_user.isChecked = filterParam.user
        cb_system.isChecked = filterParam.system
        cb_disabled.isChecked = filterParam.disabled
        cb_enabled.isChecked = filterParam.enabled
        cb_grid.isChecked = filterParam.initWithGrid
        checkGrid()

        cb_system.setOnCheckedChangeListener { buttonView, isChecked ->
            this.filterParam?.system = isChecked
            dataProvider?.request(filterParam)
        }
        cb_user.setOnCheckedChangeListener { buttonView, isChecked ->
            this.filterParam?.user = isChecked
            dataProvider?.request(filterParam)
        }
        cb_grid.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) appListView?.showInGrids() else appListView?.showInRows()
        }

        Log.d("AppFilter", "setParam $filterParam , $dataProvider")
        dataProvider?.request(filterParam)
    }

    fun setDataProvider(dataProvider: DataProvider?) {
        this.dataProvider = dataProvider
    }

    interface DataProvider {
        fun request(param: PackageFilterParam?)
    }
}
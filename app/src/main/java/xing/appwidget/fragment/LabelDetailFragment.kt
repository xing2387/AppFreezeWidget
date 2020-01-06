package xing.appwidget.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.layout_create_label.*
import xing.appwidget.R
import xing.appwidget.bean.AppInfo
import xing.appwidget.bean.PackageFilterParam
import xing.appwidget.storage.AddPackageListTask
import xing.appwidget.storage.LabelStorageHelper
import xing.appwidget.widget.AppFilter

class LabelDetailFragment(private val labelName: String?, private val editMode: Boolean) : DialogFragment(), AddPackageListTask.OnDataRequestedCallback {

    companion object {
        fun start(activity: AppCompatActivity, labelName: String?, isEditMode: Boolean) {
            LabelDetailFragment(labelName, isEditMode)
                    .show(activity.supportFragmentManager, LabelDetailFragment::class.java.simpleName)
        }
    }

    val disposeList = ArrayList<Disposable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog_FullScreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.layout_create_label, container, false)
        rootView.setOnClickListener { dismiss() }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app_list.setEdieMode(editMode)
        iv_done.setOnClickListener {
            if (editMode) {
                val labelName = et_label_name.text.toString()
                val selectedPackage = app_list.getSelectedPackageName()
                LabelStorageHelper.saveLabelSetting(labelName, selectedPackage)
            }
            dismiss()
        }
        if (editMode) {
            app_filter.visibility = View.VISIBLE
            app_filter.setAppListView(app_list)
            app_filter.setDataProvider(object : AppFilter.DataProvider {
                override fun request(param: PackageFilterParam?) {
                    AddPackageListTask(this@LabelDetailFragment).execute(param)
                }
            })
            app_filter.setParam(PackageFilterParam(user = true, initWithGrid = false))
        } else {
            app_filter.visibility = View.GONE
            val pm = context?.packageManager
            if (pm != null && labelName != null) {
                val packageNames = LabelStorageHelper.getPackageNameListByLabel(labelName)
                val dispose = LabelStorageHelper.packageNames2AppInfos(pm, packageNames)
                        .subscribeBy { app_list.setData(it) }
                disposeList.add(dispose)
            }
        }
    }

    override fun onDestroy() {
        doDispose()
        super.onDestroy()
    }

    override fun dismiss() {
        doDispose()
        super.dismissAllowingStateLoss()
    }

    private fun doDispose() {
        for (disposable in disposeList) {
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
        disposeList.clear()
    }

    override fun onAppListGet(result: List<AppInfo>) {
        app_list.setData(result)
    }

}
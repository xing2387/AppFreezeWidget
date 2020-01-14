package xing.appwidget.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_create_label.*
import xing.appwidget.R
import xing.appwidget.bean.AppInfo
import xing.appwidget.bean.PackageFilterParam
import xing.appwidget.storage.AppInfoStorageHelper
import xing.appwidget.storage.LabelStorageHelper
import xing.appwidget.widget.AppFilter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class LabelDetailFragment(private val labelName: String?, private val editMode: Boolean) : DialogFragment(), LabelManagerFragment.OnLabelSelectedListener {

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
        if (!labelName.isNullOrEmpty()) {
            et_label_name.setText(labelName)
        }
        app_list.setEdieMode(editMode)
        iv_done.setOnClickListener {
            if (editMode) {
                val labelNameInput = et_label_name.text.toString()

                if (!labelName.isNullOrEmpty() && labelName != labelNameInput) {
                    LabelStorageHelper.delLabel(labelName)
                }

                val selectedPackage = app_list.getSelectedPackageName()
                LabelStorageHelper.saveLabelSetting(labelNameInput, selectedPackage)
            }
            dismiss()
        }
        tv_select_all.setOnClickListener { app_list.selectAll() }
        tv_un_select_all.setOnClickListener { app_list.unSelectAll() }
        if (editMode) {
            app_filter.visibility = View.VISIBLE
            app_filter.setAppListView(app_list)
            app_filter.setDataProvider(object : AppFilter.DataProvider {
                override fun request(param: PackageFilterParam?) {
                    getData(false, param)
                }

                override fun openLabelList(param: PackageFilterParam?) {
                    LabelManagerFragment.start(activity as AppCompatActivity, false, this@LabelDetailFragment, app_filter.getLabels())
                }
            })
            val param = PackageFilterParam(user = true, initWithGrid = false)
            if (!labelName.isNullOrEmpty()) {
                param.labels = Collections.singleton(labelName)
            }
            getData(true, param)
            app_filter.setParam(param)
        } else {
            app_filter.visibility = View.GONE
            val pm = context?.packageManager
            if (pm != null && labelName != null) {
                val dispose = LabelStorageHelper.getAppInfoListByLabel(pm, labelName)
                        .subscribeBy(onError = { e -> Log.e("LabelDetailFragment", "packageNames2AppInfos", e) },
                                onSuccess = { appInfo -> app_list.setData(appInfo) })
                disposeList.add(dispose)
            }
        }
    }

    private fun getData(isInit: Boolean, param: PackageFilterParam?) {
        val context = context
        if (param == null || context == null) {
            return
        }
        val single1: Single<List<AppInfo>> = AppInfoStorageHelper
                .getAppInfoWithFilter(context.packageManager, param)
        val tempLabelList = HashSet<String>()
        if (!labelName.isNullOrEmpty()) {
            tempLabelList.add(labelName)
        }
        val single2: Single<List<Set<String>>> =
                if (!isInit) Single.just(ArrayList(0))
                else tempLabelList.toFlowable()
                        .map { label -> LabelStorageHelper.getPackageNameListByLabel(label) }
                        .toList()
        val biFunction = BiFunction<List<AppInfo>, List<Set<String>>, Unit> { t1, t2 ->
            if (t2.isNotEmpty()) {
                val set = HashSet<String>()
                t2.forEach { set.addAll(it) }
                app_list.setSelectedPacakgeByName(set)
            }
            app_list.setData(t1)
        }
        val disposable = Single.zip(single1, single2, biFunction)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        disposeList.add(disposable)
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

    override fun onSelected(labels: Collection<String>) {
        app_filter.setLabels(labels)
    }

}
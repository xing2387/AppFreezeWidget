package xing.appwidget.storage

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xing.appwidget.App
import xing.appwidget.bean.AppInfo
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class LabelStorageHelper {

    companion object {
        private var labelSet: MutableSet<String>? = null
        private val labelMap = HashMap<String, MutableSet<String>>()
        private val labelColorMap = HashMap<String, Int>()

        val labelSetLd = MutableLiveData<Set<String>?>()
        val labelMapLd = MutableLiveData<Map<String, Set<String>>>()
        val labelColorMapLd = MutableLiveData<Map<String, Int>>()

        fun init(context: Context) {
            if (labelSet != null) {
                return
            }
            labelSet = _getLabelSet(context)
            labelSetLd.postValue(labelSet)
            for (label in labelSet!!) {
                val packageSet = _getLabelContent(context, label)
                labelMap[label] = packageSet
            }
            labelMapLd.postValue(labelMap)
        }

        fun saveLabelSetting(labelName: String?, packageNames: Set<String>?) {
            val context = App.app?.applicationContext
            if (context == null || labelName.isNullOrBlank() || packageNames.isNullOrEmpty()) {
                return
            }
            labelSet?.add(labelName)
            labelMap[labelName] = HashSet(packageNames)
            _saveLabelPref(context, labelName, packageNames)
            labelSetLd.postValue(labelSet)
        }

        fun getPackageNameListByLabel(labelName: String): Set<String>? {
            return labelMap[labelName]
        }

        fun packageNames2AppInfos(pm: PackageManager, packageNameList: Set<String>?): Single<List<AppInfo>> {
            val packageNameList = packageNameList
                    ?: return Single.error(RuntimeException("package name list is null !!"))
            val packageInfoList = AddPackageListTask.getCachePackageInfoList()
            return Flowable.fromIterable(packageInfoList)
                    .filter { packageInfo -> packageNameList.contains(packageInfo.packageName) }
                    .map { packageInfo -> AddPackageListTask.packageInfo2AppInfo(pm, packageInfo) }
                    .toList()
                    .observeOn(Schedulers.computation())
                    .subscribeOn(AndroidSchedulers.mainThread())
        }

        /*-----------   标签相关  start -------------*/
        private fun _getLabelPref(context: Context) = context.getSharedPreferences(SharedPreferenceHelper.PREFS_LABELS, Context.MODE_PRIVATE)

        private fun _saveLabelPref(context: Context, label: String, packageNameSet: Set<String>) =
                _getLabelPref(context).edit().putStringSet(label, packageNameSet).commit()

        private fun _getLabelContent(context: Context, label: String) =
                _getLabelPref(context).getStringSet(label, Collections.emptySet()) as MutableSet<String>

        private fun _getLabelSet(context: Context) =
                _getLabelPref(context).getStringSet(SharedPreferenceHelper.PREF_PREFIX_KEY_LABLES, HashSet<String>()) as MutableSet<String>

        private fun _saveLabelSet(context: Context, labels: Set<String>) =
                _getLabelPref(context).edit().putStringSet(SharedPreferenceHelper.PREF_PREFIX_KEY_LABLES, labels).commit()
        /*-----------   标签相关  end -------------*/
    }

}
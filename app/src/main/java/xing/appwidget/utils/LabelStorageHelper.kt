package xing.appwidget.utils

import android.content.Context
import androidx.lifecycle.MutableLiveData

class LabelStorageHelper {
    companion object {
        private var labelSet: MutableSet<String>? = null
        private val labelMap = HashMap<String, MutableSet<String>>()
        private val labelColorMap = HashMap<String, Int>()

        val labelMapLd = MutableLiveData<MutableMap<String, MutableSet<String>>>()
        val labelColorMapLd = MutableLiveData<MutableMap<String, Int>>()

        fun init(contex: Context) {
            labelSet = SharedPreferenceHelper.getLabelSet(contex)
            for (label in labelSet!!) {
                val packageSet = SharedPreferenceHelper.getLabelContent(contex, label)
                labelMap[label] = packageSet
            }
            labelMapLd.postValue(labelMap)
        }
    }
}
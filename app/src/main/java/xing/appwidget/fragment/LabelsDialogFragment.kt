package xing.appwidget.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import xing.appwidget.R

class LabelsDialogFragment : DialogFragment() {

    companion object {
        fun start(activity: AppCompatActivity) {
            val fm = activity.supportFragmentManager
            LabelsDialogFragment().show(fm, LabelsDialogFragment::class.java.simpleName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog_FullScreen);
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = LayoutInflater.from(context).inflate(R.layout.layout_labels_manager, container, false)
        rootView.setOnClickListener { dismiss() }
        return rootView
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }
}
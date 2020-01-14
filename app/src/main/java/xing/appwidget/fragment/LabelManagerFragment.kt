package xing.appwidget.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.layout_label_manager.*
import xing.appwidget.R
import xing.appwidget.storage.LabelStorageHelper

class LabelManagerFragment(private val editMode: Boolean) : DialogFragment() {

    companion object {
        fun start(activity: AppCompatActivity, editMode: Boolean,
                  onLabelSelectedListener: OnLabelSelectedListener? = null,
                  selectedLabels: Collection<String>? = null) {
            val fm = activity.supportFragmentManager
            val fragment = LabelManagerFragment(editMode)
            fragment.onSelectDoneListener = onLabelSelectedListener
            fragment.selectedLabels = selectedLabels
            fragment.show(fm, LabelManagerFragment::class.java.simpleName)
        }
    }

    private var onSelectDoneListener: OnLabelSelectedListener? = null
    private var selectedLabels: Collection<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog_FullScreen)
        LabelStorageHelper.init(context!!)
        initObserver()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.layout_label_manager, container, false)
        rootView.setOnClickListener { dismiss() }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_labels.setEditMode(editMode)
        val selectedLabels = selectedLabels
        if (selectedLabels != null) {
            rv_labels.setSelected(selectedLabels)
        }
        group_edit_btns.visibility = if (editMode) View.VISIBLE else View.GONE
        iv_done.visibility = if (!editMode) View.VISIBLE else View.GONE

        iv_delete.setOnClickListener {
            if (iv_delete.isSelected) {
                for (label in rv_labels.getSelectedLabels()) {
                    LabelStorageHelper.delLabel(label)
                }
            }
            iv_delete.isSelected = !iv_delete.isSelected
        }
        iv_done.setOnClickListener {
            onSelectDoneListener?.onSelected(rv_labels.getSelectedLabels())
            dismiss()
        }
        iv_add.setOnClickListener {
            LabelDetailFragment.start(activity as AppCompatActivity, null, true)
        }
    }

    private fun initObserver() {
        LabelStorageHelper.labelSetLd.observe(this, Observer {
            if (it != null) rv_labels.setData(it)
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    interface OnLabelSelectedListener {
        fun onSelected(labels: Collection<String>)
    }
}

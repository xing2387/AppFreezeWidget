package xing.appwidget.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
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
        setStyle(STYLE_NORMAL, R.style.Dialog_FullScreen)
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

    private class ViewHolder(context: Context, parent: ViewGroup) :
            RecyclerView.ViewHolder(createItemView(context, parent)) {

        companion object {
            fun createItemView(context: Context, parent: ViewGroup): View =
                    LayoutInflater.from(context).inflate(
                            R.layout.item_label_manager, parent, false)
        }

        fun bindView(label: String) {

        }
    }

    private class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        }

        override fun getItemCount(): Int {
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        }
    }
}

package xing.appwidget.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_label_manager.view.*
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

    private lateinit var adapter: Adapter
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
        adapter = Adapter(activity!!)
        adapter.setEditMode(editMode)
        val selectedLabels = selectedLabels
        if (selectedLabels != null) {
            adapter.setSelected(selectedLabels)
        }
        group_edit_btns.visibility = if (editMode) View.VISIBLE else View.GONE
        iv_done.visibility = if (!editMode) View.VISIBLE else View.GONE
        rv_labels.layoutManager = LinearLayoutManager(context)
        rv_labels.adapter = adapter

        iv_delete.setOnClickListener {
            if (iv_delete.isSelected) {
                for (label in adapter.getSelectedLabels()) {
                    LabelStorageHelper.delLabel(label)
                }
            }
            iv_delete.isSelected = !iv_delete.isSelected
        }
        iv_done.setOnClickListener {
            onSelectDoneListener?.onSelected(adapter.getSelectedLabels())
            dismiss()
        }
        iv_add.setOnClickListener {
            LabelDetailFragment.start(activity as AppCompatActivity, null, true)
        }
    }

    private fun initObserver() {
        LabelStorageHelper.labelSetLd.observe(this, Observer {
            if (it != null) adapter.setData(it)
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    private class ViewHolder(context: Context, parent: ViewGroup) :
            RecyclerView.ViewHolder(createItemView(context, parent)) {
        var itemActionListener: ItemActionListener? = null

        companion object {
            fun createItemView(context: Context, parent: ViewGroup): View {
                val v = LayoutInflater.from(context).inflate(R.layout.item_label_manager, parent, false)
                return v
            }
        }

        fun bindView(label: String, isSelected: Boolean, editMode: Boolean) {
            itemView.tv_label_name.text = label
            itemView.cb_select.isChecked = isSelected
            itemView.cb_select.setOnCheckedChangeListener { buttonView, isChecked ->
                itemActionListener?.onCheckedChanged(isChecked, itemView.tv_label_name.text.toString())
            }
            itemView.setOnClickListener {
                itemActionListener?.onClickListener(itemView.tv_label_name.text.toString())
            }
        }

        internal interface ItemActionListener {
            fun onCheckedChanged(isChecked: Boolean, label: String)
            fun onClickListener(label: String)
        }
    }

    private class Adapter(private val context: Context) :
            RecyclerView.Adapter<ViewHolder>() {

        private val data = ArrayList<String>()
        private val selected = ArrayList<String>()
        private var editMode = false

        fun setData(data: Collection<String>) {
            this.data.clear()
            this.data.addAll(data)
            notifyDataSetChanged()
        }

        fun setEditMode(editMode: Boolean) {
            this.editMode = editMode
        }

        fun setSelected(selectedLabels: Collection<String>) {
            selected.clear()
            selected.addAll(selectedLabels)
            notifyDataSetChanged()
        }

        fun getSelectedLabels() = selected

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val viewHolder = ViewHolder(context, parent)
            viewHolder.itemActionListener = object : ViewHolder.ItemActionListener {
                override fun onCheckedChanged(isChecked: Boolean, label: String) {
                    if (isChecked) selected.add(label) else selected.remove(label)
                }

                override fun onClickListener(label: String) {
                    //Fixme context cast
                    LabelDetailFragment.start(context as AppCompatActivity, label, editMode)
                }
            }
            return viewHolder
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindView(data[position], selected.contains(data[position]), editMode)
        }
    }

    interface OnLabelSelectedListener {
        fun onSelected(labels: Collection<String>)
    }
}

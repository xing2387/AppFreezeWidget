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

class LabelManagerFragment : DialogFragment() {

    companion object {
        fun start(activity: AppCompatActivity) {
            val fm = activity.supportFragmentManager
            LabelManagerFragment().show(fm, LabelManagerFragment::class.java.simpleName)
        }
    }

    private lateinit var adapter: Adapter

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
        adapter = Adapter(context!!)
        rv_labels.layoutManager = LinearLayoutManager(context)
        rv_labels.adapter = adapter

        iv_add.setOnClickListener { LabelDetailFragment.start(activity as AppCompatActivity, false) }
    }

    private fun initObserver() {
        LabelStorageHelper.labelSetLd.observe(this, Observer {
            adapter.setData(it)
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
                v.setOnClickListener { LabelDetailFragment.start(activity, true) }
                return v
            }
        }

        fun bindView(label: String, isSelected: Boolean, editMode: Boolean) {
            itemView.tv_label_name.text = label
            itemView.cb_select.isChecked = isSelected
            itemView.cb_select.visibility = if (editMode) View.VISIBLE else View.GONE
            itemView.cb_select.setOnCheckedChangeListener { buttonView, isChecked ->
                itemActionListener?.onCheckedChanged(isChecked, itemView.tv_label_name.text.toString())
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

        fun getSelectedLabels() = selected

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val viewHolder = ViewHolder(context, parent)
            viewHolder.itemActionListener = object : ViewHolder.ItemActionListener {
                override fun onCheckedChanged(isChecked: Boolean, label: String) {
                    if (isChecked) selected.add(label) else selected.remove(label)
                }

                override fun onClickListener(label: String) {
                    LabelDetailFragment.start()
                }
            }
            return viewHolder
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindView(data[position], selected.contains(data[position]), editMode)
        }
    }
}

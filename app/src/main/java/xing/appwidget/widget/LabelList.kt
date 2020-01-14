package xing.appwidget.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_label_manager.view.*
import xing.appwidget.R
import xing.appwidget.fragment.LabelDetailFragment
import xing.appwidget.utils.Utils

class LabelList : RecyclerView {

    private var adapter: Adapter

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        layoutManager = LinearLayoutManager(context)
        adapter = Adapter(context)
        setAdapter(adapter)
    }

    fun setData(data: Collection<String>) {
        adapter.setData(data)
    }

    fun setEditMode(editMode: Boolean) {
        adapter.setEditMode(editMode)
    }

    fun setSelected(selectedLabels: Collection<String>) {
        adapter.setSelected(selectedLabels)
    }

    fun getSelectedLabels() = adapter.getSelectedLabels()

    private class Adapter(private val context: Context) : RecyclerView.Adapter<ViewHolder>() {

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
                    LabelDetailFragment.start(Utils.tryConvertToActivity(context) as AppCompatActivity, label, editMode)
                }
            }
            return viewHolder
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindView(data[position], selected.contains(data[position]), editMode)
        }
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
            with(itemView, {
                tv_label_name.text = label
                cb_select.isChecked = isSelected
                cb_select.setOnCheckedChangeListener { buttonView, isChecked ->
                    itemActionListener?.onCheckedChanged(isChecked, tv_label_name.text.toString())
                }
                setOnClickListener {
                    itemActionListener?.onClickListener(tv_label_name.text.toString())
                }
            })
        }

        internal interface ItemActionListener {
            fun onCheckedChanged(isChecked: Boolean, label: String)
            fun onClickListener(label: String)
        }
    }
}
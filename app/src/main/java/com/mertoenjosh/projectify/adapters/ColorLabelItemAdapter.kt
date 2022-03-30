package com.mertoenjosh.projectify.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mertoenjosh.projectify.R

class ColorLabelItemAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<ColorLabelItemAdapter.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_label_color, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.bind(position, item)
    }

    override fun getItemCount(): Int = list.size

    fun setClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }

    inner class ViewHolder(view: View):  RecyclerView.ViewHolder(view) {
        private val viewMain: View  = view.findViewById(R.id.viewMain)
        private val ivSelectedColor: ImageView = view.findViewById(R.id.ivSelectedColor)


        fun bind(position: Int, item: String) {
            viewMain.setBackgroundColor(Color.parseColor(item))

            if (item == mSelectedColor) {
                ivSelectedColor.visibility = View.VISIBLE
            } else {
                ivSelectedColor.visibility = View.GONE
            }

            this.itemView.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick(position, item)
                }
            }

        }

    }
}
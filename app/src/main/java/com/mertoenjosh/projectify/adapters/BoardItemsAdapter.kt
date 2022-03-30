package com.mertoenjosh.projectify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.models.Board
import de.hdodenhof.circleimageview.CircleImageView

class BoardItemsAdapter (private val context: Context, private val list: ArrayList<Board>): RecyclerView.Adapter<BoardItemsAdapter.ViewHolder>(){
    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_board, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.bind(model)

//        holder.itemView.setOnClickListener {
//            if (onClickListener != null) {
//                onClickListener!!.onClick(position, model)
//            }
//        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val boardImage: CircleImageView = view.findViewById(R.id.civItemBoardImage)
        private val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        private val tvItemCreatedBy: TextView = view.findViewById(R.id.tvItemCreatedBy)

        fun bind(model: Board) {
            Glide.with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(boardImage)

            tvItemName.text = model.name
            tvItemCreatedBy.text = "Created by: ${model.createdBy}"


            this@ViewHolder.itemView.setOnClickListener{
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }
}
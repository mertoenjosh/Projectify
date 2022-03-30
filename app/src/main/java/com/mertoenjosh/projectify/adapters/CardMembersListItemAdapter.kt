package com.mertoenjosh.projectify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.models.SelectedMember
import de.hdodenhof.circleimageview.CircleImageView

open class CardMembersListItemAdapter(
    private val context: Context,
    private val list: ArrayList<SelectedMember>,
    private val assignMembers: Boolean
) : RecyclerView.Adapter<CardMembersListItemAdapter.ViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_card_selected_member, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.bind(position, model)
    }

    override fun getItemCount(): Int = list.size

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val civSelectedMemberImage: CircleImageView = view.findViewById(R.id.civSelectedMemberImage)
        private val civAddMember: CircleImageView = view.findViewById(R.id.civAddMember)

        fun bind(position: Int, model: SelectedMember) {
            if (position == list.size - 1 && assignMembers) {
                civAddMember.visibility = View.VISIBLE
                civSelectedMemberImage.visibility = View.GONE
            } else {
                civAddMember.visibility = View.GONE
                civSelectedMemberImage.visibility = View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(civSelectedMemberImage)
            }

            this.itemView.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick(position)
                }
            }
        }

    }
}
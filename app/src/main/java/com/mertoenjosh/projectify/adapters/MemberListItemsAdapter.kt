package com.mertoenjosh.projectify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.models.User
import com.mertoenjosh.projectify.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

class MemberListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<User>,
): RecyclerView.Adapter<MemberListItemsAdapter.ViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]

        holder.bind(position, user)
    }

    override fun getItemCount(): Int = list.size


    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(position: Int, user: User, action: String)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val civMemberImage: CircleImageView = view.findViewById(R.id.civMemberImage)
        private val tvMemberName: TextView = view.findViewById(R.id.tvMemberName)
        private val tvMemberEmail: TextView = view.findViewById(R.id.tvMemberEmail)
        private val ivSelectedMember: ImageView = view.findViewById(R.id.ivSelectedMember)

        fun bind(position: Int, user: User) {
            Glide.with(context)
                .load(user.image)
                .circleCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(civMemberImage)

            tvMemberName.text = user.name
            tvMemberEmail.text = user.email

            if (user.selected) {
                ivSelectedMember.visibility = View.VISIBLE
            } else {
                ivSelectedMember.visibility = View.GONE
            }

            this.itemView.setOnClickListener {
                if (onItemClickListener != null) {
                    if (user.selected) {
                        onItemClickListener!!.onClick(position, user, Constants.UN_SELECT)
                    } else {
                        onItemClickListener!!.onClick(position, user, Constants.SELECT)
                    }
                }
            }
        }

    }

}
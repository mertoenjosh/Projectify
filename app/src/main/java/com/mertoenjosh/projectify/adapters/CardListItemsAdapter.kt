package com.mertoenjosh.projectify.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.activities.TaskListActivity
import com.mertoenjosh.projectify.models.Card
import com.mertoenjosh.projectify.models.SelectedMember

class CardListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<Card>,
): RecyclerView.Adapter<CardListItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        holder.bind(position, model)
    }

    override fun getItemCount(): Int = list.size

    // a function to bind the interface
    fun setOnCLickListener (onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(cardPosition: Int)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val tvCardName: TextView = view.findViewById(R.id.tvCardName)
        private val viewLabelColor: View = view.findViewById(R.id.viewLabelColor)
        private val rvSelectedCardMembersList: RecyclerView = view.findViewById(R.id.rvSelectedCardMembersList)

        fun bind(position: Int, model: Card) {
            tvCardName.text = model.name

            if (model.labelColor.isNotEmpty()) {
                this.viewLabelColor.visibility = View.VISIBLE
                this.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                this.viewLabelColor.visibility = View.GONE
            }

            if ((context as TaskListActivity).mAssignedMembersDetailsList.size > 0){
                val selectedMembersList: ArrayList<SelectedMember> = ArrayList()

                for (member in context.mAssignedMembersDetailsList) {
                    for (i in model.assignedTo) {
                        if (member.id == i) {
                            val selectedMember = SelectedMember(
                                member.id,
                                member.image
                            )
                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        this.rvSelectedCardMembersList.visibility = View.GONE
                    } else {
                        this.rvSelectedCardMembersList.visibility = View.VISIBLE

                        this.rvSelectedCardMembersList.layoutManager = GridLayoutManager(context, 4)

                        val adapter = CardMembersListItemAdapter(context, selectedMembersList, false)
                        this.rvSelectedCardMembersList.adapter = adapter

                        adapter.setOnClickListener( object : CardMembersListItemAdapter.OnItemClickListener {
                            override fun onClick(position: Int) {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                } else {
                    this.rvSelectedCardMembersList.visibility = View.GONE
                }

            }

            this.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }

        }

    }
}
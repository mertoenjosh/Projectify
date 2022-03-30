package com.mertoenjosh.projectify.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.adapters.MemberListItemsAdapter
import com.mertoenjosh.projectify.models.User

abstract class MembersListDialog(
    context: Context,
    private val list: ArrayList<User>,
    private val title: String = ""
): Dialog(context) {
    private var adapter: MemberListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_members_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        val tvTitle: TextView = view.findViewById(R.id.tvSelectCardMemberTitle)
        val rvMembers: RecyclerView = view.findViewById(R.id.rvCardMembersList)

        tvTitle.text = title

        if (list.size > 0) {
            rvMembers.layoutManager = LinearLayoutManager(context)
            adapter = MemberListItemsAdapter(context, list)
            rvMembers.adapter = adapter

            adapter!!.setOnClickListener( object : MemberListItemsAdapter.OnItemClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User, action: String)
}
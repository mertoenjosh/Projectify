package com.mertoenjosh.projectify.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.adapters.ColorLabelItemAdapter

abstract class ColorLabelListDialog (
    context: Context,
    private val list: ArrayList<String>,
    private var title: String = "",
    private var mSelectedColor: String = "",
) : Dialog(context){
    private var adapter: ColorLabelItemAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // current activity view group
        val viewGroup: ViewGroup = this.findViewById(android.R.id.content)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_colors_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        val tvTitle = view.findViewById<TextView>(R.id.tvSelectLabelTitle)
        val rvColorLabelList = view.findViewById<RecyclerView>(R.id.rvColorLabelList)

        tvTitle.text = title

        rvColorLabelList.layoutManager = LinearLayoutManager(context)
        adapter = ColorLabelItemAdapter(context, list, mSelectedColor)
        rvColorLabelList.adapter = adapter

        adapter!!.setClickListener(object : ColorLabelItemAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        })
    }

    protected abstract fun onItemSelected(color: String)
}
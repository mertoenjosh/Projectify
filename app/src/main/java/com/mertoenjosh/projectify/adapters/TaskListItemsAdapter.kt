package com.mertoenjosh.projectify.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.activities.TaskListActivity
import com.mertoenjosh.projectify.models.Task
import java.util.*
import kotlin.collections.ArrayList

class TaskListItemsAdapter (
    private val context: Context,
    private val list: ArrayList<Task>,
) : RecyclerView.Adapter<TaskListItemsAdapter.ViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * .7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)

        view.layoutParams = layoutParams
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        holder.bind(position, model)
    }

    override fun getItemCount(): Int = list.size

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun confirmDelete(position: Int, title: String) {
        val alertDialog = AlertDialog.Builder(context)

        // set icon
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert)
        // set alert dialog title
        alertDialog.setTitle("Alert")
        // set message
        alertDialog.setMessage("${R.string.confirmation_message_to_delete_task} '$title'")

        alertDialog.setPositiveButton("YES") { dialogInterface, _ ->
            dialogInterface.dismiss()

            if (context is TaskListActivity)
                context.deleteTaskList(position)
        }

        alertDialog.setNegativeButton("CANCEL") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        // create the dialog
        alertDialog.create()

        // set other property
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val tvAddTaskList: TextView = view.findViewById(R.id.tvAddTaskList)

        private val cvAddTaskListName: CardView = view.findViewById(R.id.cvAddTaskListName)
        private val ibCloseAddListName: ImageButton = view.findViewById(R.id.ibCloseAddListName)
        private val etTaskListName: EditText = view.findViewById(R.id.etTaskListName)
        private val ibSaveAddListName: ImageButton = view.findViewById(R.id.ibSaveAddListName)

        private val llTaskItem: LinearLayout = view.findViewById(R.id.llTaskItem)

        private val llTitleView: LinearLayout = view.findViewById(R.id.llTitleView)
        private val tvTaskListTitle: TextView = view.findViewById(R.id.tvTaskListTitle)
        private val ibEditListName: ImageButton = view.findViewById(R.id.ibEditListName)
        private val ibDeleteList: ImageButton = view.findViewById(R.id.ibDeleteList)

        private val cvEditTaskListName: CardView = view.findViewById(R.id.cvEditTaskListName)
        private val ibCloseEditListName: ImageButton = view.findViewById(R.id.ibCloseEditListName)
        private val etEditTaskListName: EditText = view.findViewById(R.id.etEditTaskListName)
        private val ibSaveEditListName: ImageButton = view.findViewById(R.id.ibSaveEditListName)

        private val cvAddCard: CardView = view.findViewById(R.id.cvAddCard)
        private val ibCloseAddCardName: ImageButton = view.findViewById(R.id.ibCloseAddCardName)
        private val etCardName: EditText = view.findViewById(R.id.etCardName)
        private val ibSaveAddCardName: ImageButton = view.findViewById(R.id.ibSaveAddCardName)

        private val tvAddCard: TextView= view.findViewById(R.id.tvAddCard)

        private val rvCardList: RecyclerView = view.findViewById(R.id.rvCardList)

        fun bind(position: Int, model: Task) {

            if (position == list.size - 1) {
                tvAddTaskList.visibility = View.VISIBLE
                llTaskItem.visibility = View.GONE
            } else {
                tvAddTaskList.visibility = View.GONE
                llTaskItem.visibility = View.VISIBLE
            }

            // set title  for the added lists
            tvTaskListTitle.text = model.title

            // set onClickListener to the addList tv
            tvAddTaskList.setOnClickListener {
                tvAddTaskList.visibility = View.GONE
                cvAddTaskListName.visibility = View.VISIBLE
            }

            // add onClick listeners to the negative btn
            ibCloseAddListName.setOnClickListener {
                etTaskListName.text.clear()
                tvAddTaskList.visibility = View.VISIBLE
                cvAddTaskListName.visibility = View.GONE
            }

            // add onClick listeners to the positive btn
            ibSaveAddListName.setOnClickListener {
                // TODO: Create entry in DB and display the task list
                val listName = etTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please enter a name", Toast.LENGTH_LONG).show()
                }
            }


            // Add EDIT or CANCEL list functionality
            ibEditListName.setOnClickListener {
                etEditTaskListName.setText(model.title)
                llTitleView.visibility = View.GONE
                cvEditTaskListName.visibility = View.VISIBLE
            }

            ibSaveEditListName.setOnClickListener {
                // TODO implement save edited
                val updatedTitle = etEditTaskListName.text.toString()
                if (updatedTitle.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, updatedTitle, model)
                    }
                } else {
                    Toast.makeText(context, "Please enter a name", Toast.LENGTH_LONG).show()
                }

            }

            ibCloseEditListName.setOnClickListener {
                llTitleView.visibility = View.VISIBLE
                cvEditTaskListName.visibility = View.GONE
            }

            // Add DELETE list functionality
            ibDeleteList.setOnClickListener {
                confirmDelete(position, model.title)
            }

            // Implement ADD card functionality
            tvAddCard.setOnClickListener {
                tvAddCard.visibility = View.GONE
                cvAddCard.visibility = View.VISIBLE
            }

            // Close add card btn
            ibCloseAddCardName.setOnClickListener {
                tvAddCard.visibility = View.VISIBLE
                cvAddCard.visibility = View.GONE
            }

            // Save add Card bnt
            ibSaveAddCardName.setOnClickListener {
                // todo save a card on DB
                val cardName = etCardName.text.toString()

                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        // TODO: ADD card to task list
                        context.addCardToTaskList(position, cardName)
                    }
                } else {
                    Toast.makeText(context, "Please enter a card name", Toast.LENGTH_SHORT).show()
                }
            }

            rvCardList.layoutManager = LinearLayoutManager(context)
            rvCardList.setHasFixedSize(true)
            val adapter = CardListItemsAdapter(context, model.cards)
            rvCardList.adapter = adapter

            adapter.setOnCLickListener( object : CardListItemsAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity) {
                        // position - TaskListPosition, cardPosition - cardPosition from CardListAdapter
                        context.cardDetails(position, cardPosition)
                    }
                }
            })

            val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            this.rvCardList.addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        dragged: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val draggedPosition = dragged.adapterPosition
                        val targetPosition = target.adapterPosition

                        if (mPositionDraggedFrom == -1) {
                            mPositionDraggedFrom = draggedPosition
                        }
                        mPositionDraggedTo = targetPosition
                        Collections.swap(list[position].cards, draggedPosition, targetPosition)
                        adapter.notifyItemMoved(draggedPosition, targetPosition)
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    }

                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)
                        if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo)
                            (context as TaskListActivity).updateCardsInTaskList(position, list[position].cards)

                        mPositionDraggedFrom = -1
                        mPositionDraggedTo = -1
                    }

                }
            )

            helper.attachToRecyclerView(this.rvCardList)
        }

    }
}
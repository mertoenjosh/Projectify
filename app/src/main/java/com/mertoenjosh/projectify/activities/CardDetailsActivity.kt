package com.mertoenjosh.projectify.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.adapters.CardMembersListItemAdapter
import com.mertoenjosh.projectify.dialogs.ColorLabelListDialog
import com.mertoenjosh.projectify.dialogs.MembersListDialog
import com.mertoenjosh.projectify.firebase.FirestoreClass
import com.mertoenjosh.projectify.models.*
import com.mertoenjosh.projectify.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {
    private lateinit var toolbarCardDetailsActivity: Toolbar
    private lateinit var etNameCardDetails: EditText
    private lateinit var btnUpdateCardDetails: Button
    private lateinit var tvSelectLabelColor: TextView
    private lateinit var tvSelectMembers: TextView
    private lateinit var rvSelectedMembersList: RecyclerView
    private lateinit var tvSelectDueDate: TextView

    private lateinit var mBoardDetails: Board
    private lateinit var mCard: Card
    private lateinit var mMembersDetailsList: ArrayList<User>

    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor = ""
    private var mSelectedDueDateMilliseconds: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        toolbarCardDetailsActivity = findViewById(R.id.toolbarCardDetailsActivity)
        etNameCardDetails = findViewById(R.id.etNameCardDetails)
        btnUpdateCardDetails = findViewById(R.id.btnUpdateCardDetails)
        tvSelectLabelColor = findViewById(R.id.tvSelectLabelColor)
        tvSelectMembers = findViewById(R.id.tvSelectMembers)
        rvSelectedMembersList = findViewById(R.id.rvSelectedMembersList)
        tvSelectDueDate = findViewById(R.id.tvSelectDueDate)

        getIntentData()

        mCard = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]
        setupActionBar(toolbarCardDetailsActivity, mCard.name)

        etNameCardDetails.setText(mCard.name)

        /* set cursor to the end after selecting the edit text */
        etNameCardDetails.setSelection(etNameCardDetails.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor

        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        btnUpdateCardDetails.setOnClickListener {
            if (etNameCardDetails.text.toString().isNotEmpty()) {
                updateCardDetails()
            } else {
                Toast.makeText(this, "A card should have a name", Toast.LENGTH_SHORT).show()
            }
        }

        tvSelectLabelColor.setOnClickListener {
            // FIXME: labelColorListDialog()
            Toast.makeText(this, "Bug not yet fixed", Toast.LENGTH_SHORT).show()
        }

        tvSelectMembers.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        mSelectedDueDateMilliseconds = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate

        if (mSelectedDueDateMilliseconds > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliseconds))
            tvSelectDueDate.text = selectedDate
        }

        tvSelectDueDate.setOnClickListener {
            showDatePicker()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionDeleteCard -> {
                confirmDelete(mCard.name)

                return true
            }
        }


        return super.onOptionsItemSelected(item)
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()

        colorsList.add("#43C86F")
        colorsList.add("#OC90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun setColor() {
        tvSelectLabelColor.text = ""
        tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun membersListDialog() {
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            for (boardMember in mMembersDetailsList) {
                for (cardMember in cardAssignedMembersList) {
                    if (boardMember.id == cardMember) {
                        boardMember.selected = true
                    }
                }
            }
        } else {
            for (boardMember in mMembersDetailsList) {
                boardMember.selected = false
            }
        }

        val membersListDialog = object : MembersListDialog(
            this@CardDetailsActivity,
            mMembersDetailsList,
            resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!mBoardDetails.taskList[mTaskListPosition]
                            .cards[mCardPosition].assignedTo.contains(user.id)) {
                        mBoardDetails.taskList[mTaskListPosition]
                            .cards[mCardPosition].assignedTo.add(user.id)
                    }
                }else {
                    mBoardDetails.taskList[mTaskListPosition]
                        .cards[mCardPosition].assignedTo.remove(user.id)

                    for (member in mMembersDetailsList) {
                        if (member.id == user.id) {
                            member.selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }
        membersListDialog.show()
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMember> = ArrayList()

        for (boardMember in mMembersDetailsList) {
            for (cardMember in cardAssignedMembersList) {
                if (boardMember.id == cardMember) {
                    val selectedMember = SelectedMember (
                        boardMember.id,
                        boardMember.image,
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMember("dummy", "member"))
            tvSelectMembers.visibility = View.GONE
            rvSelectedMembersList.visibility = View.VISIBLE

            rvSelectedMembersList.layoutManager = GridLayoutManager(this, 6)
            val adapter = CardMembersListItemAdapter(this, selectedMembersList, true)
            rvSelectedMembersList.adapter = adapter

            adapter.setOnClickListener(
                object : CardMembersListItemAdapter.OnItemClickListener {
                    override fun onClick(position: Int) {
                        membersListDialog()
                    }
                }
            )
        } else {
            tvSelectMembers.visibility = View.VISIBLE
            rvSelectedMembersList.visibility = View.GONE
        }
    }

    private fun labelColorListDialog() {
        val colorsList: ArrayList<String> = colorsList()

        val colorLabelListDialog = object : ColorLabelListDialog(
            this@CardDetailsActivity,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor
        ) {
            override fun onItemSelected(color: String) {
                 mSelectedColor = color
                 setColor()
            }
        }

        colorLabelListDialog.show()
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }

        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMembersDetailsList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun updateCardDetails() {
        val card = Card(
            etNameCardDetails.text.toString(),
            mCard.createdBy,
            mCard.assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliseconds
        )

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun deleteCard() {
        val cardList = mBoardDetails.taskList[mTaskListPosition].cards

        cardList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardList
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun confirmDelete(cardName: String) {
        val alertDialog = AlertDialog.Builder(this)

        // set icon
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert)
        // set alert dialog title
        alertDialog.setTitle(resources.getString(R.string.alert))
        // set message
        alertDialog.setMessage(resources.getString(R.string.confirmation_message_to_delete_card, cardName))

        alertDialog.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            this.deleteCard()
        }

        alertDialog.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        // create the dialog
        alertDialog.create()

        // set other property
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val date = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this,
            { _, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"

                val sMonthOfYear = if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"

                tvSelectDueDate.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliseconds = theDate!!.time
            },
            year,
            month,
            date
        )

        dpd.show()
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }
}
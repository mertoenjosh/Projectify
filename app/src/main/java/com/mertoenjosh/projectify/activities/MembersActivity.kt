package com.mertoenjosh.projectify.activities

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.adapters.MemberListItemsAdapter
import com.mertoenjosh.projectify.databinding.ActivityMembersBinding
import com.mertoenjosh.projectify.firebase.FirestoreClass
import com.mertoenjosh.projectify.models.Board
import com.mertoenjosh.projectify.models.User
import com.mertoenjosh.projectify.utils.Constants

class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMembersList: ArrayList<User>
    private var anyChangeMade = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        setupActionBar(binding.toolbarMembersActivity, resources.getString(R.string.members))

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersList(this, mBoardDetails.assignedTo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.actionAddMember -> {
                dialogSearchMember()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (anyChangeMade) {
            setResult(Activity.RESULT_OK)
        }

        super.onBackPressed()
    }


    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)

        val addMember: TextView = dialog.findViewById(R.id.tvAddMember)
        val etEmailAddMember: TextView = dialog.findViewById(R.id.etEmailAddMember)
        val tvCancel: TextView = dialog.findViewById(R.id.tvCancel)

        addMember.setOnClickListener {
            val email: String = etEmailAddMember.text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                // TODO: Search user from db
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this ,email = email)
            } else {
                Toast.makeText(this, "Please enter a member's email address", Toast.LENGTH_SHORT).show()
            }

        }

        tvCancel.setOnClickListener{
            dialog.dismiss()
        }

        dialog.setCancelable(false)
        dialog.show()
    }

    fun setupMembersList(list: ArrayList<User>) {
        mAssignedMembersList = list
        hideProgressDialog()

        binding.rvBoardMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvBoardMembersList.setHasFixedSize(true)
        val adapter = MemberListItemsAdapter(this, list)
        binding.rvBoardMembersList.adapter = adapter
    }

    fun memberDetails(user: User) {
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this, mBoardDetails, user)
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangeMade = true
        setupMembersList(mAssignedMembersList)
    }
}
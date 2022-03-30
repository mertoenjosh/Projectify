package com.mertoenjosh.projectify.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.adapters.BoardItemsAdapter
import com.mertoenjosh.projectify.firebase.FirestoreClass
import com.mertoenjosh.projectify.models.Board
import com.mertoenjosh.projectify.models.User
import com.mertoenjosh.projectify.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var headerLayout: View
    private lateinit var tvUserName: TextView
    private lateinit var civProfilePic: CircleImageView
    private lateinit var fab: FloatingActionButton
    private lateinit var rvBoardsList: RecyclerView
    private lateinit var tvNoBoards: TextView

    private var mUserName: String? = null // from lateinit

    companion object {
        const val PROFILE_UPDATE_REQUEST_CODE = 83
        const val CREATE_BOARD_REQUEST_CODE = 200
        const val TAG = "MainActivityTAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawer = findViewById(R.id.drawer_layout)

        toolbar = findViewById(R.id.toolbarMainActivity)
        setupMainActionBar(toolbar, drawer)

        navigationView = findViewById(R.id.navView)
        navigationView.setNavigationItemSelectedListener(this)
        rvBoardsList = findViewById(R.id.rvBoardsList)
        tvNoBoards = findViewById(R.id.tvNoBoards)

        // headerLayout = navigationView.getHeaderView(0)
        headerLayout = navigationView.inflateHeaderView(R.layout.activity_main_drawer_header)
        tvUserName = headerLayout.findViewById(R.id.tvUsername)
        civProfilePic = headerLayout.findViewById(R.id.civNavProfilePic)

        fab = findViewById(R.id.fab_add_board)

        FirestoreClass().getUserDetails(this, true)

        fab.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemMyProfile -> {
                val intent = Intent(this, ProfilePageActivity::class.java)
                startActivityForResult(intent, PROFILE_UPDATE_REQUEST_CODE)
            }

            R.id.itemSignOut -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            if (requestCode == PROFILE_UPDATE_REQUEST_CODE) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        FirestoreClass().getUserDetails(this)
                    }

                    Activity.RESULT_CANCELED -> {
                        Log.d(TAG, "Cancelled")
                    }

                    else -> Log.d(TAG, "Something else happened", )
                }
            } else if (requestCode == CREATE_BOARD_REQUEST_CODE) {
                when(resultCode) {
                    Activity.RESULT_OK -> {
                        FirestoreClass().getBoardsList(this)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            e.printStackTrace()
        }

    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

        if (boardsList.size > 0) {
            tvNoBoards.visibility = View.GONE
            rvBoardsList.visibility = View.VISIBLE

            // set layout manager
            rvBoardsList.layoutManager = LinearLayoutManager(this)
            rvBoardsList.setHasFixedSize(true) // increase performance

            // prepare an adapter
            val adapter = BoardItemsAdapter(this, boardsList)

            // initialize the adapter
            rvBoardsList.adapter = adapter

            // set onClickListener
            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        } else {
            tvNoBoards.visibility = View.VISIBLE
            rvBoardsList.visibility = View.GONE
        }
    }

    private fun setupMainActionBar(toolbar: Toolbar, drawer: DrawerLayout) {
        setSupportActionBar(toolbar)
        this.toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        this.toolbar.setNavigationOnClickListener {
            toggleDrawer(drawer)
        }
    }

    private fun toggleDrawer(drawer: DrawerLayout) {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    fun updateNavigationUserDetails(user: User, readBoards: Boolean) {
        mUserName = user.name
        Log.d(TAG, "$user")
        if (user.image != "") {
            Glide.with(this).load(user.image)
                .circleCrop() // fitCenter centerCrop
                .placeholder(R.drawable.ic_user_place_holder)
                .into(civProfilePic)
        }

        tvUserName.text = user.name

        if (readBoards) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

}
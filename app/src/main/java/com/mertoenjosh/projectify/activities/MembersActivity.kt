package com.mertoenjosh.projectify.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMembersList: ArrayList<User>
    private var anyChangeMade = false

    companion object {
        const val TAG = "MembersActivity"
    }

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

        SendNotificationToToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }

    private inner class SendNotificationToToUserAsyncTask(val boardName: String, val token: String)
        : AsyncTask<Any, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(resources.getString(R.string.please_wait))
        }

        override fun doInBackground(vararg params: Any?): String {
            var result: String
            var connection: HttpURLConnection? = null

            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "New Task")
                dataObject.put(Constants.FCM_KEY_MESSAGE, "Assigned to board $boardName by ${mAssignedMembersList[0].name}")

                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult: Int = connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val sb = StringBuilder()
                    var line: String?

                    try {
                        while (reader.readLine().also { line = it } != null) {
                            sb.append("$line \n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error ${e.message}"
            } finally {
                connection?.disconnect()
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
            Log.d(TAG, "$result")
        }

    }
}
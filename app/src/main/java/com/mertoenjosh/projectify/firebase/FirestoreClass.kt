package com.mertoenjosh.projectify.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.mertoenjosh.projectify.activities.*
import com.mertoenjosh.projectify.models.Board
import com.mertoenjosh.projectify.models.User
import com.mertoenjosh.projectify.utils.Constants

class FirestoreClass {

    companion object {
        private const val TAG = "FirestoreClassTAG"
    }

    private val mFirestore = FirebaseFirestore.getInstance()

    // SET user data on firestore
    fun registerUserDetails(activity: SignUpActivity, userInfo: User) {
        var didEncounterError = false
        mFirestore.collection(Constants.USERS).document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnCompleteListener { taskSave ->
                if (!taskSave.isSuccessful) {
                    didEncounterError = true
                    return@addOnCompleteListener
                }

                if (didEncounterError) {
                    Log.e(TAG, "${taskSave.exception?.message}")
                    activity.showErrorSnackBar("Error registering User")
                    return@addOnCompleteListener
                }

                if (taskSave.isSuccessful) {
                    activity.userRegisteredSuccess()
                }
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        var didEncounterError = false
        mFirestore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    didEncounterError = true
                    Log.e(TAG, "ERROR: ${task.exception?.message}")
                    task.exception?.printStackTrace()
                    return@addOnCompleteListener
                }

                if (didEncounterError) {
                    activity.showErrorSnackBar("Error while creating board")
                    return@addOnCompleteListener
                }
                activity.boardCreatedSuccessfully()
            }
    }

    // GET data from firebase
    fun getUserDetails(activity: Activity, readBoardsList: Boolean = false) {
        var didEncounterError = false
        mFirestore.collection(Constants.USERS).document(getCurrentUserId())
            .get()
            .addOnCompleteListener { getUserDataTask ->
                if (!getUserDataTask.isSuccessful) {
                    didEncounterError = true
                    Log.e(TAG, "ERROR: ${getUserDataTask.exception?.message}")
                    return@addOnCompleteListener
                }

                if (didEncounterError) {
                    when(activity) {
                        is MainActivity -> activity.hideProgressDialog()
                        is SignInActivity -> activity.hideProgressDialog()
                    }
                    return@addOnCompleteListener
                }

                val loggedInUser = getUserDataTask.result?.toObject(User::class.java)

                if (loggedInUser != null) {
                    when (activity) {
                        is SignInActivity -> activity.signInSuccess(loggedInUser)

                        is MainActivity -> activity.updateNavigationUserDetails(loggedInUser, readBoardsList)

                        is ProfilePageActivity -> activity.setUserDataInUI(loggedInUser)
                    }
                }
            }
    }

    fun getBoardsList(activity: MainActivity) {
        mFirestore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "${document.documents}")
                val boardsList = ArrayList<Board>()

                if (document.documents.size > 0) {
                    for (doc in document.documents) {
                        val board = doc.toObject(Board::class.java)!!
                        board.documentId = doc.id
                        boardsList.add(board)
                    }

                    activity.populateBoardsListToUI(boardsList)
                } else {
                    activity.hideProgressDialog()
                    Toast.makeText(activity, "No boards assigned to the user", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(TAG, "ERROR: ${e.message}",e)
            }

    }

    fun getBoardDetails(activity: TaskListActivity, boardDocumentId: String) {
        var didEncounterError = false
        mFirestore.collection(Constants.BOARDS)
            .document(boardDocumentId)
            .get()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    didEncounterError = true
                    Log.e(TAG, "${task.exception!!.message}", task.exception)
                    return@addOnCompleteListener
                }

                if (didEncounterError) {
                    activity.hideProgressDialog()
                    Toast.makeText(activity, "${task.exception!!.message}",Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val board = task.result?.toObject(Board::class.java)!!
                board.documentId = task.result!!.id
                Log.d(TAG, board.toString())
                activity.boardDetails(board)
            }

    }

    fun getAssignedMembersList(activity: Activity, assignedTo: ArrayList<String>) {
        var didEncounterError = false
        mFirestore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(TAG, "ERROR: ${task.exception?.message}", task.exception)
                    didEncounterError = true
                    return@addOnCompleteListener
                }

                if (didEncounterError) {
                    when (activity) {
                        is MembersActivity -> activity.hideProgressDialog()

                        is TaskListActivity -> activity.hideProgressDialog()
                    }
                    Toast.makeText(activity, "${task.exception!!.message}",Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val assignedUsers: ArrayList<User> = ArrayList()

                for (doc in task.result!!.documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null)
                        assignedUsers.add(user)
                }

                when (activity) {
                    is MembersActivity -> activity.setupMembersList(assignedUsers)

                    is TaskListActivity -> activity.boardMembersDetailsList(assignedUsers)
                }
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        var didEncounterError = false
        mFirestore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    didEncounterError = true
                    Log.e(TAG, "ERROR: ${task.exception!!.message}", task.exception)
                    return@addOnCompleteListener
                }

                if (didEncounterError) {
                    activity.hideProgressDialog()
                    Toast.makeText(activity, "${task.exception!!.message}",Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val documents = task.result?.documents

                if (documents?.size!! > 0) {
                    val user = documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No user with the email entered")
                }
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        Log.d(TAG, currentUser?.uid ?: "No uid")

        return currentUser?.uid ?: ""
    }

    // UPDATE data on firebase
    fun updateUserProfileData(activity: ProfilePageActivity, userHashMap: HashMap<String, Any>) {
        var didEncounterError = false
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnCompleteListener { updateTask ->
                if (!updateTask.isSuccessful) {
                    didEncounterError = true
                    Log.e(TAG, "ERROR: ${updateTask.exception!!.message}")
                    return@addOnCompleteListener
                }

                if (didEncounterError) {
                    activity.hideProgressDialog()
                    Toast.makeText(activity, "${updateTask.exception!!.message}",Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                activity.profileUpdateSuccess()
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        var didEncounterError = false

        val taskListHashMap = HashMap<String, Any>()

        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    didEncounterError = true
                    Log.e(TAG, "${task.exception?.message}", task.exception)
                    return@addOnCompleteListener
                }

                if (didEncounterError) {
                    when(activity) {
                         is TaskListActivity -> activity.hideProgressDialog()
                         is CardDetailsActivity -> activity.hideProgressDialog()
                    }

                    Toast.makeText(activity, "${task.exception!!.message}",Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                Log.d(TAG, "TASK LIST UPDATE SUCCESS")

                when (activity) {
                    is TaskListActivity -> activity.addUpdateTaskListSuccess()

                    is CardDetailsActivity -> activity.addUpdateTaskListSuccess()
                }

            }

    }

    /**
     * UPDATE the list of assignedTo in firebase
     */
    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        var didEncounterError = false
        val assignedToHashMap = HashMap<String, Any>()

        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    didEncounterError = true
                    Log.e(TAG, "ERROR: ${task.exception?.message}", task.exception)
                    return@addOnCompleteListener
                }

                if (didEncounterError) {
                    activity.hideProgressDialog()
                    Toast.makeText(activity, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                activity.memberAssignSuccess(user)
            }
    }

}

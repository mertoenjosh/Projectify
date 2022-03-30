package com.mertoenjosh.projectify.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.databinding.ActivityCreateBoardBinding
import com.mertoenjosh.projectify.firebase.FirestoreClass
import com.mertoenjosh.projectify.models.Board
import com.mertoenjosh.projectify.utils.Constants
import com.mertoenjosh.projectify.utils.isPermissionGranted
import com.mertoenjosh.projectify.utils.requestPermission
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateBoardBinding
    private lateinit var mUserName: String
    private var mBoardImage: String = ""

    private var mSelectedFileUri: Uri? = null

    companion object {
        private const val TAG = "CreateBoardActivityTAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar(binding.toolbarCreateBoardActivity, resources.getString(R.string.create_board_title))

         if (intent.hasExtra(Constants.NAME)) {
             mUserName = intent.getStringExtra(Constants.NAME)!!
         }

        binding.civCreateBoardImage.setOnClickListener {
            if (isPermissionGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Constants.showImageChooser(this)
            } else {
                requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, Constants.READ_STORAGE_REQUEST_CODE)
            }
        }

        binding.btnCreateBoard.setOnClickListener {
            if (mSelectedFileUri != null ) {
                uploadBoardImage()
            } else {
                createBoard()
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            } else {
                Toast.makeText(this, "In order to selects an image, you need to allow access to photos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
            when(resultCode) {
                Activity.RESULT_OK -> {
                    mSelectedFileUri = data?.data

                    try {
                        Glide.with(this)
                            .load(mSelectedFileUri)
                            .circleCrop()
                            .placeholder(R.drawable.ic_board_place_holder)
                            .into(binding.civCreateBoardImage)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun uploadBoardImage() {
        val filePath = "board_images/${System.currentTimeMillis()}.${Constants.getFileExtension(mSelectedFileUri!!, this)}"
        val fileReference = FirebaseStorage.getInstance().reference.child(filePath)
        var didEncounterError = false

        showProgressDialog(resources.getString(R.string.please_wait))
        fileReference.putFile(mSelectedFileUri!!)
            .continueWithTask {
                fileReference.downloadUrl
            }.addOnCompleteListener { downloadUlrTask ->

                if (!downloadUlrTask.isSuccessful) {
                    didEncounterError = true
                    Log.e(TAG, downloadUlrTask.exception!!.message.toString())
                    return@addOnCompleteListener
                }

                if (didEncounterError) {
                    hideProgressDialog()
                    showErrorSnackBar("Failed to upload board image")
                    return@addOnCompleteListener
                }

                mBoardImage = downloadUlrTask.result.toString()
                Log.d(TAG, mBoardImage)
                createBoard()
            }
    }

    private fun createBoard() {
        val boardName:String = binding.etCreateBoardName.text.toString()

        if  (validateForm(boardName)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            val assignees = ArrayList<String>()
            assignees.add(getCurrentUser_Id())
            val board = Board(name = boardName, image = mBoardImage, createdBy = mUserName, assignedTo = assignees)

            FirestoreClass().createBoard(this, board)
        }
    }

    private fun validateForm(name: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a board name")
                false
            }

            else -> true
        }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        Toast.makeText(this, "Board Created Successfully", Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

}
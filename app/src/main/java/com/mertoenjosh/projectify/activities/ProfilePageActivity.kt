package com.mertoenjosh.projectify.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.databinding.ActivityProfilePageBinding
import com.mertoenjosh.projectify.firebase.FirestoreClass
import com.mertoenjosh.projectify.models.User
import com.mertoenjosh.projectify.utils.Constants
import com.mertoenjosh.projectify.utils.isPermissionGranted
import com.mertoenjosh.projectify.utils.requestPermission
import java.io.IOException

class ProfilePageActivity : BaseActivity() {
    private lateinit var binding: ActivityProfilePageBinding
    private lateinit var mUserDetails: User
    private var mSelectedFileUri: Uri? = null
    private var mProfileImageUrl: String = ""

    companion object {
        private const val TAG = "ProfilePageActivityTAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar(binding.toolbarProfilePageActivity, null)

        FirestoreClass().getUserDetails(this)

        binding.civProfilePic.setOnClickListener {
            if (isPermissionGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Constants.showImageChooser(this)
            } else {
                requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, Constants.READ_STORAGE_REQUEST_CODE)
            }
        }


        binding.btnUpdateProfile.setOnClickListener {
            if (mSelectedFileUri != null) {
                uploadUserImage()
            } else {
                updateUserProfileData()
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
            when (resultCode){
                Activity.RESULT_OK -> {
                    mSelectedFileUri = data?.data
                    Log.d(TAG, "$mSelectedFileUri")


                    try {
                        Glide.with(this)
                            .load(mSelectedFileUri)
                            .circleCrop()
                            .placeholder(R.drawable.ic_user_place_holder)
                            .into(binding.civProfilePic)
                    } catch (e: IOException) {
                        Log.e(TAG, "${e.message}")
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    private fun uploadUserImage() {
        if (mSelectedFileUri != null) {
            var didEncounterError = false
            showProgressDialog(resources.getString(R.string.please_wait))
            val filePath = "profile_images/${mUserDetails.id}/${System.currentTimeMillis()}.${Constants.getFileExtension(mSelectedFileUri!!, this)}"
            val photoReference = FirebaseStorage.getInstance().reference.child(filePath)

            photoReference.putFile(mSelectedFileUri!!)
                .continueWithTask { photoUploadTask ->
                    Log.i(TAG, "Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                    photoReference.downloadUrl
                }.addOnCompleteListener { urlDownloadTask ->

                    if (!urlDownloadTask.isSuccessful) {
                        Log.e(TAG, "Exception with firebase storage", urlDownloadTask.exception)
                        didEncounterError = true
                        return@addOnCompleteListener
                    }

                    if (didEncounterError) {
                        hideProgressDialog()
                        Toast.makeText(this, getString(R.string.profile_update_failure), Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    mProfileImageUrl = urlDownloadTask.result.toString()
                    updateUserProfileData()
                    Toast.makeText(this, getString(R.string.profile_update_success), Toast.LENGTH_LONG).show()
                    Log.d(TAG, mProfileImageUrl)
                }

            // Alternatively

            /**
            photoReference.putFile(mSelectedFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "${taskSnapshot.metadata?.reference?.downloadUrl}")

                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    Log.d(TAG, "$uri")

                    mProfileImageUrl = "$uri"

                    hideProgressDialog()
                    // TODO: updateUserProfileData
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "${exception.message}", Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }

            */
        }
    }

    private fun updateUserProfileData() {
        val userHashMap =  HashMap<String, Any>()
        var anyChangeMade = false
        val name = binding.etProfileName.text.toString()
        val mobile = binding.etProfileMobile.text.toString().toLong()

        if (validateName(name)) {

            if (mProfileImageUrl.isNotEmpty() && mProfileImageUrl != mUserDetails.image) {
                userHashMap[Constants.IMAGE] = mProfileImageUrl
                Log.d(TAG, mProfileImageUrl)
                anyChangeMade = true
            }

            if (name.isNotEmpty() && name != mUserDetails.name) {
                userHashMap[Constants.NAME] = binding.etProfileName.text.toString()
                anyChangeMade = true
            }

            if (mobile != mUserDetails.mobile) {
                userHashMap[Constants.MOBILE] = binding.etProfileMobile.text.toString().toLong()
                anyChangeMade = true
            }

            if (anyChangeMade) {
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().updateUserProfileData(this@ProfilePageActivity, userHashMap)
            } else {
                Toast.makeText(this, getString(R.string.profile_no_change_made), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateName(name: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar(resources.getString(R.string.error_empty_name))
                false
            }

            else -> true
        }
    }



    fun setUserDataInUI(user: User) {

        mUserDetails = user

        Glide.with(this)
            .load(mUserDetails.image)
            .circleCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.civProfilePic)

        binding.etProfileName.setText(mUserDetails.name)
        binding.etProfileEmail.setText(mUserDetails.email)

        if(mUserDetails.mobile != 0L)
            binding.etProfileMobile.setText(mUserDetails.mobile.toString())
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        Toast.makeText(this, resources.getString(R.string.profile_update_success), Toast.LENGTH_SHORT)
            .show()
        Log.d(TAG, "Profile data updated successfully")
        setResult(Activity.RESULT_OK)
        finish()
    }

}
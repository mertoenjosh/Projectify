package com.mertoenjosh.projectify.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.databinding.ActivitySignInBinding
import com.mertoenjosh.projectify.firebase.FirestoreClass
import com.mertoenjosh.projectify.models.User

class SignInActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "SignInActivityTAG"
    }

    private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar(binding.toolbarSignInActivity, null)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        auth = FirebaseAuth.getInstance()

        binding.btnSignIn.setOnClickListener {
            signInUser()
        }
    }

    private fun signInUser() {
        val email = binding.etEmail.text.toString().trim { it <= ' ' }
        val password = binding.etName.text.toString().trim { it <= ' ' }

        if  (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    FirestoreClass().getUserDetails(this)
                } else {
                    Log.d(TAG, "Login not successful")
                    showErrorSnackBar(task.exception?.message.toString())
                }
            }

        }
    }

    fun signInSuccess(loggedInUser: User) {
        hideProgressDialog()
        Toast.makeText(this, "Logged in user", Toast.LENGTH_SHORT).show()
        Log.d(TAG, loggedInUser.toString())
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(resources.getString(R.string.error_empty_email))
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(resources.getString(R.string.error_empty_password))
                false
            }


            else -> true
        }
    }

}
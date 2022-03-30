package com.mertoenjosh.projectify.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mertoenjosh.projectify.R
import com.mertoenjosh.projectify.databinding.ActivitySignUpBinding
import com.mertoenjosh.projectify.firebase.FirestoreClass
import com.mertoenjosh.projectify.models.User

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar(binding.toolbarSignUpActivity, null)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }

    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim { it <= ' ' }
        val email = binding.etEmail.text.toString().trim{ it <= ' ' }
        val password = binding.etPassword.text.toString().trim{ it <= ' ' }

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->

                    if(!task.isSuccessful) {
                        hideProgressDialog()
                        showErrorSnackBar(task.exception?.message.toString())
                        return@addOnCompleteListener
                    }


                    val firebaseUser : FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!

                    val user = User(firebaseUser.uid, name, registeredEmail)

                    FirestoreClass().registerUserDetails(this, user)

                }
        }
    }

    fun userRegisteredSuccess() {
        hideProgressDialog()
        Toast.makeText(this, "User Registered successfully", Toast.LENGTH_LONG).show()

        FirebaseAuth.getInstance().signOut()
        finish()

    }

    private fun validateForm(name: String, email: String, password: String): Boolean {

        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar(resources.getString(R.string.error_empty_name))
                false
            }

            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(resources.getString(R.string.error_empty_email))
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(resources.getString(R.string.error_empty_password))
                return false
            }
            else -> true
        }
    }
}
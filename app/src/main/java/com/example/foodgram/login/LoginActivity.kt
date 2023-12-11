package com.example.foodgram.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.foodgram.AuthManager
import com.example.foodgram.BaseActivity
import com.example.foodgram.MainActivity
import com.example.foodgram.R
import com.example.foodgram.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Loading
    private lateinit var overlay: View
    private lateinit var progress: ProgressBar

    private lateinit var emailTextField: EditText
    private lateinit var passwordTextField: EditText
    private lateinit var passwordConfirmTextField: EditText
    private lateinit var usernameTextField: EditText

    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var guestButton: Button

    private lateinit var forgotPasswordButton: TextView

    private lateinit var registerSwitch: TextView
    private lateinit var loginSwitch: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
        setupListeners()

        // Login is available in the beginning
        navigateToLogin()

        // Hide app bar
        supportActionBar?.hide()
    }

    override fun clearFocusFromAllForms() {
        usernameTextField.clearFocus()
        emailTextField.clearFocus()
        passwordTextField.clearFocus()
        passwordConfirmTextField.clearFocus()
    }

    private fun initializeViews() {
        overlay =  binding.overlay
        progress = binding.progressBar

        emailTextField = binding.emailTextField.editText!!
        passwordTextField = binding.passwordTextField.editText!!
        passwordConfirmTextField = binding.passwordConfirmTextField.editText!!
        usernameTextField = binding.usernameTextField.editText!!

        loginButton = binding.loginButton
        registerButton = binding.registerButton
        guestButton = binding.guestButton

        forgotPasswordButton = binding.forgotPasswordTextView

        loginSwitch = binding.loginTextView
        registerSwitch = binding.registerTextView
    }

    private fun setupListeners() {
        loginButton.setOnClickListener { onLoginButtonClicked() }
        registerButton.setOnClickListener { onRegisterButtonClicked() }
        guestButton.setOnClickListener{ navigateToMain() }

        registerSwitch.setOnClickListener { navigateToRegistration() }
        loginSwitch.setOnClickListener { navigateToLogin() }

        forgotPasswordButton.setOnClickListener { showForgotPasswordDialog() }
    }

    private fun onLoginButtonClicked() {
        val email = emailTextField.text.toString()
        val password = passwordTextField.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            login(email, password)
        } else {
            showEmptyFieldsError()
        }
    }

    private fun login(email: String, password: String) {

        loadingStart()

        AuthManager.signIn(email, password) { isSuccess ->
            if (isSuccess) {
                onLoginSuccess()
            } else {
                onLoginFailure()
            }
        }
    }

    private fun onRegisterButtonClicked() {
        val email = emailTextField.text.toString()
        val password = passwordTextField.text.toString()
        val passwordConfirm = passwordConfirmTextField.text.toString()
        val username = usernameTextField.text.toString()

        if (email.isNotEmpty() && username.isNotEmpty()
            && password.isNotEmpty() && passwordConfirm.isNotEmpty()) {
            if (password == passwordConfirm) {
                register(email, password, username)
            } else {
                showPasswordMismatch()
            }
        } else {
            showEmptyFieldsError()
        }
    }

    private fun register(email: String,
                         password: String,
                         username: String) {

        loadingStart()

        AuthManager.register(email, password, username) { isSuccess ->
            if (isSuccess) {
                //login(email, password)
                onRegistrationSuccess()
            } else {
                onRegistrationFailure()
            }
        }
    }

    private fun onLoginSuccess() {
        loadingEnd()
        navigateToMain()
    }

    private fun navigateToMain() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun onLoginFailure() {
        loadingEnd()
        showToast(getString(R.string.wrong_credentials))
    }

    private fun showEmptyFieldsError() {
        showToast(getString(R.string.empty_fields_error))
    }

    private fun showPasswordMismatch() {
        showToast(getString(R.string.password_mismatch_error))
    }

    private fun onRegistrationSuccess() {
        loadingEnd()
        showToast(getString(R.string.registration_successful))
        navigateToMain()
    }

    private fun onRegistrationFailure() {
        loadingEnd()
        showToast(getString(R.string.registration_failed))
    }


    private fun showToast(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToRegistration() {
        binding.loginTitleLayout.visibility = View.GONE
        binding.loginButtonLayout.visibility = View.GONE
        binding.registerTitleLayout.visibility = View.VISIBLE
        binding.registerButtonLayout.visibility = View.VISIBLE
        binding.passwordConfirmTextField.visibility = View.VISIBLE
        binding.usernameTextField.visibility = View.VISIBLE
    }

    private fun navigateToLogin() {
        binding.loginTitleLayout.visibility = View.VISIBLE
        binding.loginButtonLayout.visibility = View.VISIBLE
        binding.registerTitleLayout.visibility = View.GONE
        binding.registerButtonLayout.visibility = View.GONE
        binding.passwordConfirmTextField.visibility = View.GONE
        binding.usernameTextField.visibility = View.GONE
    }

    private fun showForgotPasswordDialog() {
        // TODO
    }

    private fun loadingStart() {
        overlay.visibility = View.VISIBLE
        progress.visibility = View.VISIBLE
    }

    private fun loadingEnd() {
        overlay.visibility = View.GONE
        progress.visibility = View.GONE
    }
}

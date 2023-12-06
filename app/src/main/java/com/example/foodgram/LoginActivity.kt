package com.example.foodgram

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodgram.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

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

    private fun initializeViews() {
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
            AuthManager.signIn(email, password) { isSuccess ->
                if (isSuccess) {
                    onLoginSuccess()
                } else {
                    onLoginFailure()
                }
            }
        } else {
            showEmptyFieldsError()
        }
    }

    private fun onRegisterButtonClicked() {
        val email = emailTextField.text.toString()
        val password = passwordTextField.text.toString()
        val passwordConfirm = passwordConfirmTextField.text.toString()
        val username = usernameTextField.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && password.isNotEmpty()) {
            if (password == passwordConfirm) {
                AuthManager.register(email, password, username) { isSuccess ->
                    if (isSuccess) {
                        onRegistrationSuccess()
                    } else {
                        onRegistrationFailure()
                    }
                }
            } else {
                showPasswordMismatch()
            }
        } else {
            showEmptyFieldsError()
        }
    }

    private fun onLoginSuccess() {
        navigateToMain()
    }

    private fun navigateToMain() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun onLoginFailure() {
        showToast(getString(R.string.wrong_credentials))
    }

    private fun showEmptyFieldsError() {
        showToast(getString(R.string.empty_fields_error))
    }

    private fun showPasswordMismatch() {
        showToast(getString(R.string.password_mismatch_error))
    }

    private fun onRegistrationSuccess() {
        showToast(getString(R.string.registration_successful))
        navigateToMain()
    }

    private fun onRegistrationFailure() {
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
        // Implement forgot password functionality (e.g., show a dialog)
    }
}

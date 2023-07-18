package com.JuseungL.justagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var emailLoginButton: AppCompatButton
    //private lateinit var emailLoginButton: AppCompatButton
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.email_edittext)
        passwordEditText = findViewById(R.id.password_edittext)
        emailLoginButton = findViewById<AppCompatButton>(R.id.email_login_button)
        emailLoginButton.setOnClickListener{
            signinAndSignup()
        }
    }
    //회원가입 하고 만약 기존 가입자 일 경우 그대로 로그인
    fun signinAndSignup()
    {
        auth?.createUserWithEmailAndPassword(
            emailEditText.text.toString(),
            passwordEditText.text.toString()
        )?.addOnCompleteListener {
            task ->
                if(task.isSuccessful){
                    //Creating new account
                    moveMainPage(task.result.user)
                } else if(task.exception?.message.isNullOrEmpty()){
                    //show the error message
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    //login if you have account
                    signinEmail()
                }

        }
    }

    //로그인
    fun signinEmail() {
        auth?.signInWithEmailAndPassword(
            emailEditText.text.toString(),
            passwordEditText.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //Login
                moveMainPage(task.result.user)
            } else {
                //show the error message
                Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    //로그인 또는 회원가입 성공 시 메인 페이지로 이동
    fun moveMainPage(user:FirebaseUser?) {
        if(user!=null) {
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}
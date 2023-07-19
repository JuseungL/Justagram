package com.JuseungL.justagram

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    // 뷰와 파이어베이스 인증 변수를 정의합니다.
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var googleSignInButton: AppCompatButton
    private lateinit var emailLoginButton: AppCompatButton
    private var auth: FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null

    // 구글 로그인 요청 코드를 상수로 정의합니다.
    companion object {
        const val GOOGLE_LOGIN_CODE = 1234
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 파이어베이스 인증을 초기화합니다.
        auth = FirebaseAuth.getInstance()

        // 뷰를 각각의 ID와 연결합니다.
        emailEditText = findViewById(R.id.email_edittext)
        passwordEditText = findViewById(R.id.password_edittext)
        emailLoginButton = findViewById(R.id.email_login_button)
        googleSignInButton = findViewById(R.id.google_sign_in_button)

        // 버튼에 대한 클릭 리스너를 설정합니다.
        emailLoginButton.setOnClickListener {
            signinAndSignup()
        }
        googleSignInButton.setOnClickListener {
            googleLogin()
        }

        // 구글 로그인 옵션을 설정합니다.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // gso에 의해 지정된 옵션으로 GoogleSignInClient를 초기화합니다.
        googleSignInClient = GoogleSignIn.getClient(this,gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 구글 로그인을 처리합니다.
        if (requestCode == GOOGLE_LOGIN_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // 구글 로그인이 성공하면, 파이어베이스 인증을 진행합니다.
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // 구글 로그인이 실패하면, 에러 메시지를 표시합니다.
                Toast.makeText(this, "구글 로그인 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun googleLogin() {
        // 구글 로그인 과정을 시작합니다.
        val signInIntent = googleSignInClient?.signInIntent
        signInIntent?.let { startActivityForResult(it, GOOGLE_LOGIN_CODE) }
    }

    fun signinAndSignup() {
        // 사용자 계정을 생성하거나 이미 존재하는 계정으로 로그인을 시도합니다.
        auth?.createUserWithEmailAndPassword(
            emailEditText.text.toString(),
            passwordEditText.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                moveMainPage(task.result?.user)
            } else if (task.exception?.message.isNullOrEmpty()) {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            } else {
                signinEmail()
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        // 구글 계정으로 인증 토큰을 얻어와 파이어베이스 인증을 진행합니다.
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 로그인이 성공하면, 로그인한 사용자 정보를 업데이트합니다.
                val user = auth?.currentUser
                moveMainPage(user)
            } else {
                // 로그인이 실패하면, 사용자에게 메시지를 표시합니다.
                Toast.makeText(this, "인증 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun signinEmail() {
        // 이메일과 패스워드를 이용해 로그인을 시도합니다.
        auth?.signInWithEmailAndPassword(
            emailEditText.text.toString(),
            passwordEditText.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                moveMainPage(task.result?.user)
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun moveMainPage(user: FirebaseUser?) {
        // 로그인한 사용자가 있으면, 메인 페이지로 이동합니다.
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}

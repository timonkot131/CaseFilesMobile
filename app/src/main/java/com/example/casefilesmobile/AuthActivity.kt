package com.example.casefilesmobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.casefilesmobile.network_operations.Authorization
import cz.msebera.android.httpclient.client.HttpClient
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        toRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        enterButton.setOnClickListener {
            Authorization.login(scope, loginInput.text.toString(), pwdInput.text.toString()) {
                when(it.code){
                    200 -> Toast.makeText(this, "you have entered", Toast.LENGTH_SHORT).show()
                    404 -> Toast.makeText(this, "nice try", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
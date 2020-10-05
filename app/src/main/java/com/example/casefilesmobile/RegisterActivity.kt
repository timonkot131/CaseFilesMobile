package com.example.casefilesmobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.casefilesmobile.network_operations.Authorization
import com.example.casefilesmobile.network_operations.Registration
import com.example.casefilesmobile.pojo.Account
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_auth.enterButton
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.MainScope
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        enterButton.setOnClickListener {
            val acc = Account(
                0,
                loginField.toString(),
                pwdField.toString(),
                ageField.toString().toInt(),
                Calendar.getInstance().timeInMillis,
                nameField.toString(),
                secondNameField.toString()
            )

            Registration.register(scope, acc) {
                when (it.code) {
                    200 -> Toast.makeText(this, "Congratulations", Toast.LENGTH_SHORT).show()
                    409 -> Toast.makeText(this, "somebody already picked up login", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
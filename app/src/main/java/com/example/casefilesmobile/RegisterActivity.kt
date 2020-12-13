package com.example.casefilesmobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.casefilesmobile.network_operations.Registration
import com.example.casefilesmobile.pojo.Account
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerButton.setOnClickListener {
            val acc = Account(
                0,
                loginField.text.toString(),
                pwdField.text.toString(),
                ageField.text.toString().toInt(),
                Calendar.getInstance().timeInMillis,
                nameField.text.toString(),
                secondNameField.text.toString()
            )

            scope.launch {
                Registration.register(acc) {
                    when (it.code) {
                        200 -> {
                            val intent =
                                Intent(this@RegisterActivity, ExploringCasesActivity::class.java)
                            intent.putExtra(CaseViewActivity.USER_ID, it.account!!.id)
                            startActivity(intent)
                        }
                        409 -> Toast.makeText(
                            this@RegisterActivity,
                            "somebody already picked up login",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}

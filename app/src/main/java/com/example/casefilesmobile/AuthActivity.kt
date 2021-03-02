package com.example.casefilesmobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.casefilesmobile.network_operations.Authorization
import kotlinx.android.synthetic.main.activity_auth.*
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
            scope.launch {
                Authorization.login(loginInput.text.toString(), pwdInput.text.toString()) {
                    when (it.code) {
                        200 -> {
                            val intent =
                                Intent(this@AuthActivity, ExploringCasesActivity::class.java)
                            intent.putExtra(CaseViewActivity.USER_ID, it.account!!.id)
                            startActivity(intent)
                        }
                        404 -> Toast.makeText(this@AuthActivity, getString(R.string.NiceTry), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }
}
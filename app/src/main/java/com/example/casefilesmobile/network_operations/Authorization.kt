package com.example.casefilesmobile.network_operations

import com.example.casefilesmobile.pojo.Account
import com.example.casefilesmobile.pojo.AccountResponse
import com.google.gson.Gson
import cz.msebera.android.httpclient.client.HttpClient
import cz.msebera.android.httpclient.client.entity.EntityBuilder
import cz.msebera.android.httpclient.client.methods.HttpPost
import cz.msebera.android.httpclient.entity.ContentType
import cz.msebera.android.httpclient.impl.client.HttpClients
import cz.msebera.android.httpclient.util.EntityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class Authorization {

    companion object {
        fun login(
            scope: CoroutineScope,
            login: String,
            pwd: String,
            onComplete: (r: AccountResponse) -> Unit
        ) {
            scope.launch(Dispatchers.Default) {
                val gson = Gson()

                val client = HttpClients.createDefault() as HttpClient

                val json =
                    JSONObject()
                        .put("login", login)
                        .put("pwd", pwd).toString()

                val request = HttpPost("http://10.0.3.2:5000/api/cases/login")
                request.entity = EntityBuilder.create()
                    .setText(json)
                    .setContentType(ContentType.APPLICATION_JSON)
                    .build()


                val response = client.execute(request)

                scope.launch(Dispatchers.Main) {
                    when (response.statusLine.statusCode) {
                        200 -> onComplete(
                            AccountResponse(
                                gson.fromJson(
                                    EntityUtils.toString(response.entity),
                                    Account::class.java
                                ), 200
                            )
                        )
                        404 -> onComplete(AccountResponse(null, 404))
                    }
                }
            }
        }
    }
}
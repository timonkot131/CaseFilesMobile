package com.example.casefilesmobile.network_operations

import com.example.casefilesmobile.pojo.Account
import com.example.casefilesmobile.pojo.AccountResponse
import com.example.casefilesmobile.stringifyAsync
import com.example.casefilesmobile.withAsync
import com.google.gson.Gson
import cz.msebera.android.httpclient.client.HttpClient
import cz.msebera.android.httpclient.client.entity.EntityBuilder
import cz.msebera.android.httpclient.client.methods.HttpPost
import cz.msebera.android.httpclient.entity.ContentType
import cz.msebera.android.httpclient.impl.client.HttpClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class Authorization {

    companion object {
        suspend fun login(
            login: String,
            pwd: String,
            onComplete: (r: AccountResponse) -> Unit
        ) {
            withContext(Dispatchers.Default) {
                val gson = Gson()

                val client = HttpClients.createDefault() as HttpClient

                val json =
                    JSONObject()
                        .put("login", login)
                        .put("pwd", pwd).toString()

                val request = HttpPost("http://10.0.3.2:44370/api/cases/login")
                request.entity = EntityBuilder.create()
                    .setText(json)
                    .setContentEncoding("utf-8")
                    .setContentType(ContentType.APPLICATION_JSON)
                    .build()

                val response = withAsync(Dispatchers.IO) { client.execute(request) }

                withContext(Dispatchers.Main) {
                    when (response.await().statusLine.statusCode) {
                        200 -> onComplete(
                            AccountResponse(
                                gson.fromJson(
                                    response.await().entity.stringifyAsync().await(),
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
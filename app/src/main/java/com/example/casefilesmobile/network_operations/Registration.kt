package com.example.casefilesmobile.network_operations

import com.example.casefilesmobile.pojo.Account
import com.example.casefilesmobile.pojo.AccountResponse
import com.example.casefilesmobile.withAsync
import com.google.gson.Gson
import cz.msebera.android.httpclient.client.HttpClient
import cz.msebera.android.httpclient.client.entity.EntityBuilder
import cz.msebera.android.httpclient.client.methods.HttpPost
import cz.msebera.android.httpclient.entity.ContentType
import cz.msebera.android.httpclient.impl.client.HttpClients
import cz.msebera.android.httpclient.util.EntityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Registration {
    companion object {
        suspend fun register(account: Account, onComplete: (r: AccountResponse) -> Unit) {
            withContext(Dispatchers.Default) {
                val gson = Gson()

                val client = HttpClients.createDefault() as HttpClient

                val json = gson.toJson(account)

                val request = HttpPost("http://10.0.3.2:44370/api/cases/register")
                request.entity = EntityBuilder.create()
                    .setText(json)
                    .setContentEncoding("utf-8")
                    .setContentType(ContentType.APPLICATION_JSON)
                    .build()

                val response = withAsync(Dispatchers.IO) { client.execute(request) }
                val entityString =
                    withAsync(Dispatchers.IO) { EntityUtils.toString(response.await().entity) }

                withContext(Dispatchers.Main) {
                    when (response.await().statusLine.statusCode) {
                        200 -> onComplete(
                            AccountResponse(
                                gson.fromJson(
                                    entityString.await(),
                                    Account::class.java
                                ), 200
                            )
                        )
                        409 -> onComplete(AccountResponse(null, 409))
                    }
                }

            }
        }
    }

}
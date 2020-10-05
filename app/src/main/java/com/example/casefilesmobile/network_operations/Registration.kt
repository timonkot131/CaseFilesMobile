package com.example.casefilesmobile.network_operations

import com.example.casefilesmobile.pojo.Account
import com.example.casefilesmobile.pojo.AccountResponse
import com.google.gson.Gson
import cz.msebera.android.httpclient.HttpRequest
import cz.msebera.android.httpclient.client.HttpClient
import cz.msebera.android.httpclient.client.entity.EntityBuilder
import cz.msebera.android.httpclient.client.methods.HttpPost
import cz.msebera.android.httpclient.client.methods.RequestBuilder
import cz.msebera.android.httpclient.impl.client.HttpClients
import cz.msebera.android.httpclient.util.EntityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Registration {
    companion object {
        fun register (scope: CoroutineScope, account: Account, onComplete: (r: AccountResponse) -> Unit) {
            scope.launch(Dispatchers.Default) {
                val gson = Gson()

                val client = HttpClients.createDefault() as HttpClient

                val json = gson.toJson(account)

                val request = HttpPost("https://localhost:44370/api/cases/register")
                request.entity = EntityBuilder.create().setText(json).build()

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
                        409 -> onComplete(AccountResponse(null, 409))
                    }
                }

            }
        }
    }

}
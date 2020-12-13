package com.example.casefilesmobile.network_operations

import com.example.casefilesmobile.pojo.BigCase
import com.example.casefilesmobile.pojo.ShortCase
import com.example.casefilesmobile.pojo.TrackingCase
import com.example.casefilesmobile.withAsync
import com.google.gson.Gson
import cz.msebera.android.httpclient.client.entity.EntityBuilder
import cz.msebera.android.httpclient.client.methods.HttpPost
import cz.msebera.android.httpclient.client.utils.URIBuilder
import cz.msebera.android.httpclient.impl.client.HttpClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URLEncoder

class TrackingCases {
    companion object {
        private val gson = Gson()

        fun getUri(userId: Int, page: Int, size: Int?): URI {
            val builder = URIBuilder()
                .setScheme("http")
                .setHost("10.0.3.2:44370/api/cases/trackedCases/" + userId)
                .addParameter("page", page.toString())

            size?.let {
                builder.addParameter("size", it.toString())
            }

            return builder.build()
        }

        suspend fun addCase(userId: Int, shortCase: ShortCase, bigCase: BigCase) {
            withContext(Dispatchers.IO) {
                val client = HttpClients.createDefault()
                val post = HttpPost("http://10.0.3.2:44370/api/cases/trackedCases/" + userId)

                val encoded = withAsync(Dispatchers.IO) {
                    val json = bigCase.getJson()
                    URLEncoder.encode(json.toString(), "utf-8")
                }

                post.setHeader("Content-Type", "application/json; charset=utf-8")
                val case = TrackingCase(
                    0,
                    shortCase.registrationDate.time,
                    shortCase.court,
                    shortCase.region,
                    shortCase.number,
                    encoded.await()
                )

                post.entity = EntityBuilder.create()
                    .setText(gson.toJson(case))
                    .setContentEncoding("utf-8").build()

                client.execute(post)
            }
        }
    }
}

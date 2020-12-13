package com.example.casefilesmobile.network_operations

import com.example.casefilesmobile.pojo.CourtCode
import com.example.casefilesmobile.pojo.RegionCode
import com.example.casefilesmobile.stringifyAsync
import com.example.casefilesmobile.withAsync
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.msebera.android.httpclient.client.HttpClient
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.client.utils.URIBuilder
import cz.msebera.android.httpclient.impl.client.HttpClients
import kotlinx.coroutines.Dispatchers

class Codes {
    companion object {
        private val gson = Gson()
        suspend fun getRegions(): List<RegionCode> {
            val client = HttpClients.createDefault() as HttpClient
            val request = HttpGet("http://10.0.3.2:44370/api/codes/regions")
            val res = withAsync(Dispatchers.IO) { client.execute(request) }

            return gson.fromJson<Array<RegionCode>>(
                res.await().entity.stringifyAsync().await(),
                object : TypeToken<Array<RegionCode>>() {}.type
            ).asList()
        }

        suspend fun getCourts(reg: String): List<CourtCode> {
            val client = HttpClients.createDefault() as HttpClient

            val builder = URIBuilder()
                .setScheme("http")
                .setHost("10.0.3.2:44370/api/codes/courts")
                .addParameter("region", reg)

            val request = HttpGet(builder.build())
            val res = withAsync(Dispatchers.IO) { client.execute(request) }

            return gson.fromJson<Array<CourtCode>>(
                res.await().entity.stringifyAsync().await(),
                object : TypeToken<Array<CourtCode>>() {}.type
            ).asList()
        }
    }
}
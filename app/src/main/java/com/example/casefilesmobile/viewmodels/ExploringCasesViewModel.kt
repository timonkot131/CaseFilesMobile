package com.example.casefilesmobile.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casefilesmobile.adapters.UTCAdapter
import com.example.casefilesmobile.network_operations.TrackingCases
import com.example.casefilesmobile.pojo.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.client.utils.URIBuilder
import cz.msebera.android.httpclient.impl.client.HttpClients
import cz.msebera.android.httpclient.util.EntityUtils
import kotlinx.coroutines.*
import java.util.*
import kotlin.reflect.KSuspendFunction0

class ExploringCasesViewModel : ViewModel() {
    val gson: Gson = GsonBuilder().registerTypeAdapter(Date::class.java, UTCAdapter()).create()

    val cases: MutableLiveData<ShortCaseResponse> by lazy {
        MutableLiveData<ShortCaseResponse>()
    }

    private val userId: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply { value = null }
    }

    private val query: MutableLiveData<CaseQuery> by lazy {
        MutableLiveData<CaseQuery>().apply { value = null }
    }

    private fun getUri(query: CaseQuery) = URIBuilder()
        .setScheme("http")
        .setHost("10.0.3.2:44370/api/cases")
        .setCharset(Charsets.UTF_8)
        .addParameter("region", query.region)
        .addParameter("courtName", query.court)
        .addParameter("page", query.page.toString())
        .addParameter("number", query.number)
        .addParameter("side", query.side)
        .addParameter("dateTo", query.dateTo.toString())
        .addParameter("dateFrom", query.dateFrom.toString())
        .build()

    private fun attachJob(shortCase: ShortCase): ShortCase {
        val job: () -> Deferred<BigCase?> = {
            viewModelScope.async(Dispatchers.IO) {
                val client = HttpClients.createDefault()

                val uri = URIBuilder()
                    .setScheme("http")
                    .setHost("10.0.3.2:44370/api/cases/moreInfo")
                    .setCharset(Charsets.UTF_8)
                    .addParameter("court", shortCase.court)
                    .addParameter("region", shortCase.region)
                    .addParameter("number", shortCase.number)
                    .build()

                val res = client.execute(HttpGet(uri))
                when (res.statusLine.statusCode) {
                    200 -> BigCase.parseJson(EntityUtils.toString(res.entity))
                    else -> null
                }
            }
        }
        shortCase.bigCaseJob = job
        return shortCase
    }

    fun requestCases(query: CaseQuery?, id: Int?) =
        viewModelScope.launch(Dispatchers.IO) {
            query?.let {
                val client = HttpClients.createDefault()
                val getShort = HttpGet(getUri(it))

                val tracked = id?.let {
                    try {
                        val getTrack = HttpGet(TrackingCases.getUri(id, 0, 1000))
                        val trackRes = client.execute(getTrack)
                        when (trackRes.statusLine.statusCode) {
                            200 -> gson.fromJson<Array<TrackingCase>>(
                                EntityUtils.toString(trackRes.entity),
                                object : TypeToken<Array<TrackingCase>>() {}.type
                            ).asList()
                            else -> null
                        }
                    } catch (ex: Exception) {
                        null
                    }
                }

                try {
                    val res = client.execute(getShort)
                    when (res.statusLine.statusCode) {
                        200 -> {
                            val json = gson.fromJson<Array<ShortCase>>(
                                EntityUtils.toString(res.entity),
                                object : TypeToken<Array<ShortCase>>() {}.type
                            ).asList().map(::attachJob)
                            val shorts =
                                tracked?.let { json.filter { j -> !tracked.any { t -> t.number == j.number } } }
                            val filteredRes = shorts?.let { ShortCaseResponse(it, 200) }
                            val resp = filteredRes ?: ShortCaseResponse(json, 200)
                            withContext(Dispatchers.Main) { cases.value = resp }
                        }

                        else -> withContext(Dispatchers.Main) {
                            cases.value = ShortCaseResponse(arrayOf<ShortCase>().asList(), 204)
                        }
                    }
                } catch (ex: Exception) {
                    withContext(Dispatchers.Main) {
                        cases.value = ShortCaseResponse(arrayOf<ShortCase>().asList(), 500)
                    }
                }
            }
        }

    fun requestCases() {
        requestCases(query.value, userId.value)
    }

    fun nextPage() {
        query.value?.let {
            query.value = it.copy(page = it.page + 1)
        }
    }

    fun previousPage() {
        query.value?.let {
            query.value = it.copy(page = it.page - 1)
        }
    }
}
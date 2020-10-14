package com.example.casefilesmobile.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casefilesmobile.POJO.BigCase
import com.example.casefilesmobile.network_operations.TrackingCases
import com.example.casefilesmobile.pojo.CaseQuery
import com.example.casefilesmobile.pojo.ShortCase
import com.example.casefilesmobile.pojo.ShortCaseResponse
import com.example.casefilesmobile.pojo.TrackingCase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.client.utils.URIBuilder
import cz.msebera.android.httpclient.impl.client.HttpClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.reflect.Type

class ExploringCasesViewModel() : ViewModel() {
    val gson: Gson = Gson()

    val cases: MutableLiveData<ShortCaseResponse> by lazy {
        MutableLiveData<ShortCaseResponse>()
    }

    private val userId: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply { value = null }
    }

    private val query: MutableLiveData<CaseQuery> by lazy {
        MutableLiveData<CaseQuery>().apply { value = null }
    }

    fun getUri(query: CaseQuery) = URIBuilder()
        .setPath("http://10.0.3.2:5000/api/cases")
        .setCharset(Charsets.UTF_8)
        .addParameter("page", query.page.toString())
        .addParameter("size", query.pageSize.toString())
        .addParameter("number", query.number)
        .addParameter("side", query.side)
        .addParameter("toDate", query.to.toString())
        .addParameter("fromDate", query.from.toString())
        .build()


    fun attachJob(shortCase: ShortCase): ShortCase {
        val job: Deferred<BigCase?> = viewModelScope.async(Dispatchers.Default) {
            val client = HttpClients.createDefault()

            val uri = URIBuilder()
                .setPath("http://10.0.3.2:5000/api/cases/moreInfo")
                .setCharset(Charsets.UTF_8)
                .addParameter("court", shortCase.court)
                .addParameter("number", shortCase.number)
                .build()

            val res = client.execute(HttpGet(uri))
            when (res.statusLine.statusCode) {
                200 -> BigCase.parseJson(res.entity.toString())
                else -> null
            }
        }
        shortCase.bigCaseJob = job
        return shortCase
    }

    fun requestCases(query: CaseQuery?, id: Int?) =
        viewModelScope.launch(Dispatchers.Default) {
            query?.let {
                val client = HttpClients.createDefault()
                val getShort = HttpGet(getUri(it))

                val tracked = id?.let {
                    try {
                        val getTrack = HttpGet(TrackingCases.getUri(id, 0, 1000))
                        val trackRes = client.execute(getTrack)
                        when (trackRes.statusLine.statusCode) {
                            200 -> gson.fromJson<Array<TrackingCase>>(
                                trackRes.entity.toString(),
                                object : TypeToken<Array<TrackingCase>>() {}.type
                            ).asList()
                            else -> null
                        }
                    }
                    catch(ex: Exception) {
                        null
                    }
                }

                try {
                    val res = client.execute(getShort)
                    when (res.statusLine.statusCode) {
                        200 -> {
                            val json = gson.fromJson<Array<ShortCase>>(
                                res.entity.toString(),
                                object : TypeToken<Array<ShortCase>>() {}.type
                            ).asList().map(::attachJob)
                            val shorts =
                                tracked?.let { json.filter { j -> !tracked.any { t -> t.number == j.number } } }
                            val filteredRes = shorts?.let { ShortCaseResponse(it, 200) }
                            val resp = filteredRes ?: ShortCaseResponse(json, 200)
                            cases.value = resp
                        }

                        404 -> cases.value = ShortCaseResponse(arrayOf<ShortCase>().asList(), 204)
                    }
                }
                catch (ex: Exception){
                    cases.value = ShortCaseResponse(arrayOf<ShortCase>().asList(), 500)
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
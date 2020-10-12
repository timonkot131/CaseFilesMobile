package com.example.casefilesmobile.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casefilesmobile.pojo.CaseQuery
import com.example.casefilesmobile.pojo.ShortCase
import com.example.casefilesmobile.pojo.ShortCaseResponse
import com.example.casefilesmobile.pojo.Table
import com.example.casefilesmobile.toList
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import cz.msebera.android.httpclient.client.entity.EntityBuilder
import cz.msebera.android.httpclient.client.methods.HttpEntityEnclosingRequestBase
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.client.utils.URIBuilder
import cz.msebera.android.httpclient.entity.ContentType
import cz.msebera.android.httpclient.impl.client.HttpClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

class ExploringCasesViewModel() : ViewModel() {
    val gson: Gson = Gson()

    val cases: MutableLiveData<ShortCaseResponse> by lazy {
        MutableLiveData<ShortCaseResponse>()
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

    fun parseCommonTable(array: JSONArray): Table {
        val headers = array.toList()
            .first()
            .keys()
            .asSequence()
            .toList()

        val body =
            array
                .toList()
                .toTypedArray()
                .map { it to it.keys().asSequence() }
                .map { pair ->
                    pair.second.map { pair.first.getString(it) }.toList()
                }

        return Table(headers, body)
    }

    fun parseJson(text: String) {
        val body = JSONObject(text)
        val type = body.getString("caseType")

        val mainDataJson: JSONObject? =
            if(!body.isNull("mainData"))
              body.getJSONObject("mainData")
            else null

        val sides: JSONArray? =
            if(!body.isNull("sides"))
                body.getJSONArray("sides")
            else null

        val events: JSONArray? =
            if(!body.isNull("events"))
                body.getJSONArray("events")
            else null

        val mainData =
            mainDataJson?.keys()?.asSequence()?.map {
                val data = mainDataJson.getString(it)
                it to data
            }?.toList()
                ?.map { listOf(it.first, it.second) }


        var mainDataTable: Table? = mainData?.let {
            Table(listOf("",""), it)
        }

        val eventsDataTable: Table? = events?.let{
            parseCommonTable(it)
        }

        val sidesDataTable: Table? = sides?.let{
            parseCommonTable(it)
        }



    }

    fun requestCases(query: CaseQuery?) =
        viewModelScope.launch(Dispatchers.Default) {
            query?.let {
                val client = HttpClients.createDefault()
                val get = HttpGet(getUri(it))
                val res = client.execute(get)

                when (res.statusLine.statusCode) {
                    200 -> cases.value = ShortCaseResponse(
                        gson.fromJson(
                            res.entity.toString(),
                            Array<ShortCase>::class.java
                        ).asList(), 200
                    )
                    404 -> cases.value = ShortCaseResponse(arrayOf<ShortCase>().asList(), 204)
                }
            }

        }

    fun requestCases() {
        requestCases(query.value)
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
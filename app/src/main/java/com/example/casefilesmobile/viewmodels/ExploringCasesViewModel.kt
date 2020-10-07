package com.example.casefilesmobile.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casefilesmobile.pojo.CaseQuery
import com.example.casefilesmobile.pojo.ExploringCase
import com.example.casefilesmobile.pojo.ExploringResponse
import com.google.gson.Gson
import cz.msebera.android.httpclient.client.entity.EntityBuilder
import cz.msebera.android.httpclient.client.methods.HttpEntityEnclosingRequestBase
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.entity.ContentType
import cz.msebera.android.httpclient.impl.client.HttpClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI

class ExploringCasesViewModel() : ViewModel() {
    val gson: Gson = Gson()

    val cases: MutableLiveData<ExploringResponse> by lazy {
        MutableLiveData<ExploringResponse>()
    }

    private val query: MutableLiveData<CaseQuery> by lazy {
        MutableLiveData<CaseQuery>().apply { value = null }
    }

    fun requestCases(query: CaseQuery?) =
        viewModelScope.launch(Dispatchers.Default) {
            val json =  gson.toJson(query)
            val client = HttpClients.createDefault()

            val get = object : HttpEntityEnclosingRequestBase() {
                init{
                    uri = URI("http://10.0.3.2:5000/api/cases/login")
                    entity = EntityBuilder.create()
                        .setText(json)
                        .setContentType(ContentType.APPLICATION_JSON)
                        .build()
                }

                override fun getMethod(): String? = "GET"
            }
            val res = client.execute(get)

            when(res.statusLine.statusCode) {
                200 -> cases.value = ExploringResponse(gson.fromJson(res.entity.toString(), ExploringCase::class.java))
            }
        }



    fun requestCases() {
        requestCases(query.value)
    }
}
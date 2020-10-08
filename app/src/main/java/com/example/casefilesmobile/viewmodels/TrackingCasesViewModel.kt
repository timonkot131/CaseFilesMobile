package com.example.casefilesmobile.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casefilesmobile.pojo.*
import com.google.gson.Gson
import cz.msebera.android.httpclient.client.entity.EntityBuilder
import cz.msebera.android.httpclient.client.methods.HttpEntityEnclosingRequestBase
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.entity.ContentType
import cz.msebera.android.httpclient.impl.client.HttpClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI

class TrackingCasesViewModel() : ViewModel() {

    val gson: Gson = Gson()

    val cases: MutableLiveData<TrackingResponse> by lazy {
        MutableLiveData<TrackingResponse>()
    }

    private val userId: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply { value = null }
    }

    fun requestCases(userId: Int?) =
        viewModelScope.launch(Dispatchers.Default) {
            val client = HttpClients.createDefault()

            val get = HttpGet("http://10.0.3.2:5000/api/cases/trackedcases/" + userId)
            val res = client.execute(get)

            when (res.statusLine.statusCode) {
                200 -> cases.value = TrackingResponse(
                    gson.fromJson(
                        res.entity.toString(),
                        Array<TrackingCase>::class.java
                    ).asList(), 200
                )
                204 -> cases.value = TrackingResponse(null, 204)
            }
        }

    fun requestCases() {
        requestCases(userId.value)
    }
}
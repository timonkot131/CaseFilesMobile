package com.example.casefilesmobile.viewmodels

import android.content.Entity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casefilesmobile.network_operations.TrackingCases.Companion.getUri
import com.example.casefilesmobile.pojo.*
import com.google.gson.Gson
import cz.msebera.android.httpclient.HttpResponse
import cz.msebera.android.httpclient.client.entity.EntityBuilder
import cz.msebera.android.httpclient.client.methods.HttpEntityEnclosingRequestBase
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.client.utils.URIBuilder
import cz.msebera.android.httpclient.entity.ContentType
import cz.msebera.android.httpclient.impl.client.HttpClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI

import com.example.casefilesmobile.network_operations.TrackingCases
import com.google.gson.reflect.TypeToken
import cz.msebera.android.httpclient.HttpEntity
import cz.msebera.android.httpclient.util.EntityUtils
import kotlinx.coroutines.async

class TrackingCasesViewModel() : ViewModel() {

    val gson: Gson = Gson()

    val cases: MutableLiveData<TrackingResponse> by lazy {
        MutableLiveData<TrackingResponse>()
    }

    private val userId: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply { value = null }
    }

    private val page: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply { value = 0 }
    }

    private val size: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply { value = null }
    }

    fun stringEntity(e: HttpEntity) = viewModelScope.async(Dispatchers.Default) {
        EntityUtils.toString(e)
    }

    fun handleResponse(res: HttpResponse) =
        viewModelScope.launch(Dispatchers.Main) {
            when (res.statusLine.statusCode) {
                200 -> cases.value = TrackingResponse(

                    gson.fromJson<Array<TrackingCase>>(
                        stringEntity(res.entity).await(),
                        object : TypeToken<Array<TrackingCase>>() {}.type
                    ).asList(), 200
                )
                204 -> cases.value = TrackingResponse(arrayOf<TrackingCase>().asList(), 204)
                else -> cases.value =
                    TrackingResponse(arrayOf<TrackingCase>().asList(), res.statusLine.statusCode)
            }
        }

    private fun requestCases(userId: Int, page: Int?, size: Int?) {
        this.page.value = page ?: 0
        viewModelScope.launch(Dispatchers.Default) {
            val client = HttpClients.createDefault()
            val get = HttpGet(TrackingCases.getUri(userId, page ?: 0, size))
            handleResponse(client.execute(get))
        }
    }


    fun requestCases(userId: Int) {
        requestCases(userId, page.value, 10)

    }

    fun nextPage() {
        page.value?.let{
            page.value = it + 1
        }
    }

    fun previousPage(){
        page.value?.let{
            if (it <= 0) return@let
            page.value = it - 1
        }
    }
}
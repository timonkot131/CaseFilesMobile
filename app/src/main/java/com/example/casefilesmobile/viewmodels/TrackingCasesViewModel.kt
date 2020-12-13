package com.example.casefilesmobile.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casefilesmobile.network_operations.TrackingCases
import com.example.casefilesmobile.pojo.TrackingCase
import com.example.casefilesmobile.pojo.TrackingResponse
import com.example.casefilesmobile.stringifyAsync
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.msebera.android.httpclient.HttpResponse
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.impl.client.HttpClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackingCasesViewModel : ViewModel() {

    val gson: Gson = Gson()

    val cases: MutableLiveData<TrackingResponse> by lazy {
        MutableLiveData<TrackingResponse>()
    }

    private val page: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply { value = 0 }
    }

    private fun handleResponse(res: HttpResponse) =
        viewModelScope.launch(Dispatchers.Main) {
            when (res.statusLine.statusCode) {
                200 -> cases.value = TrackingResponse(

                    gson.fromJson<Array<TrackingCase>>(
                        res.entity.stringifyAsync().await(),
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

        viewModelScope.launch(Dispatchers.IO) {
            val client = HttpClients.createDefault()
            val get = HttpGet(TrackingCases.getUri(userId, page ?: 0, size))
            handleResponse(client.execute(get))
        }
    }

    fun requestCases(userId: Int) {
        requestCases(userId, page.value, 999)
    }

}
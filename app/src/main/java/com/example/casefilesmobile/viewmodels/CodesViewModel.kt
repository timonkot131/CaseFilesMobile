package com.example.casefilesmobile.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.casefilesmobile.network_operations.Codes
import com.example.casefilesmobile.pojo.CourtCode
import com.example.casefilesmobile.pojo.RegionCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CodesViewModel : ViewModel() {

    private val scope = MainScope()

    val regions: MutableLiveData<List<RegionCode>> by lazy {
        MutableLiveData<List<RegionCode>>().apply { value = listOf() }
    }

    val courts: MutableLiveData<List<CourtCode>> by lazy {
        MutableLiveData<List<CourtCode>>().apply { value = listOf() }
    }

    fun getRegions() =
        scope.launch(Dispatchers.IO) {
            val regs = Codes.getRegions()
            withContext(Dispatchers.Main) {
                regions.value = regs
            }
        }

    fun getCourts(region: RegionCode) =
        scope.launch(Dispatchers.IO) {
            val regs = Codes.getCourts(region.code)
            withContext(Dispatchers.Main) {
                courts.value = regs
            }
        }
}
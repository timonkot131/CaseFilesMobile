package com.example.casefilesmobile.pojo

import android.os.Bundle

data class Table(val headers: List<String>, val body: List<List<String>>){
    fun fromBundle(bundle: Bundle?, id:String){
        val bund = bundle?.getBundle(id)
        va

    }
}
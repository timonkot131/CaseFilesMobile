package com.example.casefilesmobile.POJO

import android.os.Bundle
import com.example.casefilesmobile.toArrayList
import java.util.*

data class Table(val headers: List<String>, val body: List<List<String>>) {

    fun pushToBundle(bundle: Bundle?, id: String){
        val b = Bundle()
        b.putStringArrayList(HEADERS, headers.toArrayList())
        b.putInt(ROW_COUNT, body.count())
        for (i in 0 until body.count()){
            b.putStringArrayList(i.toString(), body[i].toArrayList())
        }
        bundle?.putBundle(id, b)
    }

    companion object{
        const val HEADERS = "headers"
        const val ROW_COUNT = "rowsCount"

        fun fromBundle(bundle: Bundle?, id: String): Table? {
            val bund = bundle?.getBundle(id)
            val headers = bund?.getStringArrayList(HEADERS)?.toList()
            val rowsCount = bund?.getInt(ROW_COUNT)

            var rows = rowsCount?.let {
                sequence<List<String>> {
                    for (i in 0 until rowsCount) yield(
                        bund.getStringArrayList(
                            i.toString()
                        )!!.toList()
                    )
                }.toList()
            }

            return headers?.let { h->
                rows?.let {r ->
                    Table(h, r)
                }
            }
        }
    }
}
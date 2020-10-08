package com.example.casefilesmobile

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.json.JSONArray
import org.json.JSONObject

fun JSONArray.toList(): List<JSONObject> {
    val result = mutableListOf<JSONObject>()

    for (i in 0 until this.length()) {
        result.add(this.getJSONObject(i))
    }

    return result
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, func: (T) -> Unit) {
    observe(owner, Observer<T>(func))
}

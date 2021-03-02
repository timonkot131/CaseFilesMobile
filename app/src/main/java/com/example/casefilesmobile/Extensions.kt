package com.example.casefilesmobile

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.DatePicker
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import cz.msebera.android.httpclient.HttpEntity
import cz.msebera.android.httpclient.util.EntityUtils
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

fun JSONArray.toList(): List<JSONObject> {
    val result = mutableListOf<JSONObject>()

    for (i in 0 until this.length()) {
        result.add(this.getJSONObject(i))
    }

    return result
}

fun ViewGroup.addViews(views: List<View>){
    repeat(views.count()){
        this.addView(views[it])
    }
}

fun <T> List<T>.toArrayList(): ArrayList<T> {
    return arrayListOf<T>().apply {
        addAll(this@toArrayList)
    }
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, func: (T) -> Unit) {
    observe(owner, Observer<T>(func))
}

val DatePicker.epochTicks : Long
    get() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        return calendar.time.time
    }

suspend fun HttpEntity.stringifyAsync() = withAsync(Dispatchers.IO) {
    EntityUtils.toString(this@stringifyAsync)
}


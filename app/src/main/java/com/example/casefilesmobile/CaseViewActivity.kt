package com.example.casefilesmobile

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.example.casefilesmobile.network_operations.TrackingCases
import com.example.casefilesmobile.pojo.BigCase
import com.example.casefilesmobile.pojo.ShortCase
import com.example.casefilesmobile.pojo.Table
import com.example.casefilesmobile.pojo.TrackingCase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import cz.msebera.android.httpclient.client.methods.HttpDelete
import cz.msebera.android.httpclient.impl.client.HttpClients
import kotlinx.android.synthetic.main.activity_case_view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CaseViewActivity : AppCompatActivity() {
    private val gson = Gson()

    private var shortCase: ShortCase? = null
    private var trackingCase: TrackingCase? = null
    private var userId = 0

    private var afterClickJob: Job? = null

    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_case_view)

        val mainData = Table.fromBundle(intent.extras, MAINDATA)
        val events = Table.fromBundle(intent.extras, EVENTS)
        val sides = Table.fromBundle(intent.extras, SIDES)
        var caseType: String? = ""

        intent.extras?.run {
            shortCase = getParcelable<ShortCase>(SHORT_CASE)
            trackingCase = getParcelable<TrackingCase>(TRACKING_CASE)
            userId = getInt(USER_ID)
            caseType = getString(CASE_TYPE)
        }

        trackingCase?.run {
            viewFab.setImageResource(R.drawable.ic_baseline_remove_24)
            viewFab.setOnClickListener(::onTrackingFabClick)
        }

        shortCase?.let {
            viewFab.setOnClickListener { _ ->
                val bigCase = BigCase(caseType!!, mainData, sides, events)
                scope.launch(Dispatchers.IO) {
                    TrackingCases.addCase(userId, it, bigCase)
                }
                viewFab.hide()
            }
        }

        mainData?.let {
            mainDataTable.columnCount = it.headers.count()
            mainDataTable.addViews(mapToElems(it.body.flatten()))
        }

        events?.let {
            eventsTable.columnCount = it.headers.count()
            eventsTable.addViews(mapToElems(it.headers))
            eventsTable.addViews(mapToElems(it.body.flatten()))
        }

        sides?.let {
            sidesTable.columnCount = it.headers.count()
            sidesTable.addViews(mapToElems(it.headers))
            sidesTable.addViews(mapToElems(it.body.flatten()))
        }
    }

    private fun mapToElems(list: List<String>) =
        list.map {
            TextView(this).apply {
                text = it
                setPadding(10)
            }
        }

    private fun onTrackingFabClick(v: View) {
        v as FloatingActionButton
        scope.launch(Dispatchers.IO) {
            val client = HttpClients.createDefault()
            val delete =
                HttpDelete("http://10.0.3.2:44370/api/cases/trackedCases/" + trackingCase!!.id)
            client.execute(delete)
        }
        v.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        afterClickJob?.cancel()
    }

    companion object {
        const val CASE_TYPE = "caseType"
        const val USER_ID = "userId"
        const val SHORT_CASE = "shortCase"
        const val TRACKING_CASE = "trackingCase"
        const val EVENTS = "events"
        const val SIDES = "sides"
        const val MAINDATA = "maindata"
    }
}
package com.example.casefilesmobile

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.casefilesmobile.adapters.ExploringRecyclerAdapter
import com.example.casefilesmobile.adapters.TrackingRecyclerAdapter
import com.example.casefilesmobile.pojo.*
import com.example.casefilesmobile.viewmodels.ExploringCasesViewModel
import com.example.casefilesmobile.viewmodels.TrackingCasesViewModel
import kotlinx.android.synthetic.main.activity_exploring_cases.*
import kotlinx.android.synthetic.main.activity_tracking.*
import kotlinx.android.synthetic.main.alert_search.*

class TrackingActivity : AppCompatActivity() {
    private val model: TrackingCasesViewModel by viewModels()
    private var adapter: TrackingRecyclerAdapter? = null

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        savedInstanceState?.run {
            userId = getInt(USER_ID)
        }

        model.cases.observe(this, ::onCasesObserve)

        trackingRecycler.layoutManager = LinearLayoutManager(applicationContext)

        model.requestCases(userId)
    }

    private fun onTrackingClick(case: TrackingCase) {
        startActivity(Intent(this, CaseViewActivity::class.java))
    }

    private fun updateCases(cases: List<TrackingCase>) {
        if (trackingRecycler.adapter == null) {
            trackingRecycler.adapter =
                TrackingRecyclerAdapter(
                    applicationContext,
                    cases.toMutableList(),
                    ::onTrackingClick
                )
            adapter = exploringRecycler.adapter as TrackingRecyclerAdapter
        }

        adapter?.update(cases)
    }

    private fun showMessage(text: String) {
        adapter?.clear()
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun onCasesObserve(response: TrackingResponse) =
        when (response.code) {
            200 -> updateCases(response.cases)
            else -> showMessage("Не удалось найти дела по указанному запросу")
        }

    companion object{
        const val USER_ID = "user_id"
    }
}
package com.example.casefilesmobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.casefilesmobile.adapters.TrackingRecyclerAdapter
import com.example.casefilesmobile.pojo.BigCase
import com.example.casefilesmobile.pojo.TrackingCase
import com.example.casefilesmobile.pojo.TrackingResponse
import com.example.casefilesmobile.viewmodels.TrackingCasesViewModel
import kotlinx.android.synthetic.main.activity_tracking.*
import java.net.URLDecoder

class TrackingActivity : AppCompatActivity() {
    private val model: TrackingCasesViewModel by viewModels()
    private var adapter: TrackingRecyclerAdapter? = null

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        intent.extras?.run {
            userId = getInt(USER_ID)
        }


        model.cases.observe(this, ::onCasesObserve)

        trackingRecycler.layoutManager = LinearLayoutManager(applicationContext)

        model.requestCases(userId)
        trackingProgressBar.isIndeterminate = true
    }

    private fun onTrackingClick(case: TrackingCase) {
        val decodedJson = URLDecoder.decode(case.json, "utf-8")
        val bigCase = BigCase.parseJson(decodedJson)

        val intent = Intent(this, CaseViewActivity::class.java)
        intent.putExtra(CaseViewActivity.TRACKING_CASE, case)
        intent.putExtra(CaseViewActivity.USER_ID, userId)
        intent.putExtra(CaseViewActivity.CASE_TYPE, bigCase.caseType)
        bigCase.mainData?.pushToBundle(intent, CaseViewActivity.MAINDATA)
        bigCase.sides?.pushToBundle(intent, CaseViewActivity.SIDES)
        bigCase.events?.pushToBundle(intent, CaseViewActivity.EVENTS)
        startActivity(intent)
    }

    private fun updateCases(cases: List<TrackingCase>) {
        if (trackingRecycler.adapter == null) {
            trackingRecycler.adapter =
                TrackingRecyclerAdapter(
                    applicationContext,
                    cases.toMutableList(),
                    ::onTrackingClick
                )
            adapter = trackingRecycler.adapter as TrackingRecyclerAdapter
        }

        adapter?.update(cases)
    }

    private fun showMessage(text: String) {
        adapter?.clear()
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun onCasesObserve(response: TrackingResponse) {
        trackingProgressBar.isIndeterminate = false
        when (response.code) {
            200 -> updateCases(response.cases)
            else -> showMessage(getString(R.string.CantFindCases))
        }
    }

    companion object {
        const val USER_ID = "user_id"
    }
}
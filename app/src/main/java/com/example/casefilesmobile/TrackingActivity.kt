package com.example.casefilesmobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.casefilesmobile.pojo.BigCase
import com.example.casefilesmobile.adapters.TrackingRecyclerAdapter
import com.example.casefilesmobile.pojo.*
import com.example.casefilesmobile.viewmodels.TrackingCasesViewModel
import kotlinx.android.synthetic.main.activity_exploring_cases.*
import kotlinx.android.synthetic.main.activity_tracking.*
import java.net.URLDecoder

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
        val decodedJson = URLDecoder.decode(case.json, "utf-8")
        val bigCase = BigCase.parseJson(decodedJson)

        val intent = Intent(this, CaseViewActivity::class.java)
        val bundle = intent.extras
        bundle?.putParcelable(CaseViewActivity.DATA, case)
        bundle?.putInt(CaseViewActivity.USER_ID, userId)
        bundle?.putString(CaseViewActivity.CASE_TYPE, bigCase.caseType)
        bigCase.mainData?.pushToBundle(bundle, CaseViewActivity.MAINDATA)
        bigCase.sides?.pushToBundle(bundle, CaseViewActivity.SIDES)
        bigCase.events?.pushToBundle(bundle, CaseViewActivity.EVENTS)

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
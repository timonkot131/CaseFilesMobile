package com.example.casefilesmobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.casefilesmobile.adapters.ExploringRecyclerAdapter
import com.example.casefilesmobile.pojo.ShortCase
import com.example.casefilesmobile.pojo.ShortCaseResponse
import com.example.casefilesmobile.viewmodels.ExploringCasesViewModel
import kotlinx.android.synthetic.main.activity_exploring_cases.*

class ExploringCasesActivity : AppCompatActivity() {

    private val model: ExploringCasesViewModel by viewModels()
    private var adapter: ExploringRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exploring_cases)

        model.cases.observe(this, ::onCasesObserve)

        exploringRecycler.layoutManager = LinearLayoutManager(applicationContext)
    }

    private fun onExploringClick(case: ShortCase) {
        startActivity(Intent(this, CaseViewActivity::class.java))
    }

    private fun updateCases(cases: List<ShortCase>) {
        if (exploringRecycler.adapter == null) {
            exploringRecycler.adapter =
                ExploringRecyclerAdapter(
                    applicationContext,
                    cases.toMutableList(),
                    ::onExploringClick
                )
            adapter = exploringRecycler.adapter as ExploringRecyclerAdapter
        }

        adapter?.update(cases)
    }

    private fun showMessage(text: String) {
        adapter?.clear()
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun onCasesObserve(response: ShortCaseResponse) =
        when (response.code) {
            200 -> updateCases(response.cases)
            else -> showMessage("Не удалось найти дела по указанному запросу")
        }
}
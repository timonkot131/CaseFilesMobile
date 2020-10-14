package com.example.casefilesmobile

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.casefilesmobile.adapters.ExploringRecyclerAdapter
import com.example.casefilesmobile.pojo.CaseQuery
import com.example.casefilesmobile.pojo.ShortCase
import com.example.casefilesmobile.pojo.ShortCaseResponse
import com.example.casefilesmobile.viewmodels.ExploringCasesViewModel
import kotlinx.android.synthetic.main.activity_exploring_cases.*
import kotlinx.android.synthetic.main.alert_search.*
import kotlinx.coroutines.runBlocking

class ExploringCasesActivity : AppCompatActivity() {

    private val model: ExploringCasesViewModel by viewModels()
    private var adapter: ExploringRecyclerAdapter? = null

    private var userId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exploring_cases)

        model.cases.observe(this, ::onCasesObserve)

        userId = intent.extras?.getInt(CaseViewActivity.USER_ID)

        exploringRecycler.layoutManager = LinearLayoutManager(applicationContext)

        exploringFab.setOnClickListener(::BuildDialog)

        model.requestCases()

        exploringBottomBar.setNavigationIcon(R.drawable.ic_baseline_exit_to_app_24)
        exploringBottomBar.setNavigationOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
        }

        exploringBottomBar.setOnCreateContextMenuListener { menu, v, menuInfo ->
            menuInflater.inflate(R.menu.exploring_bottom_bar_menu, menu)
        }

        exploringBottomBar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.app_bar_bottomTracking -> {
                    val intent = Intent(this, TrackingActivity::class.java)
                    intent.putExtra(CaseViewActivity.USER_ID, userId)
                    startActivity(intent)
                }
            }

            true
        }
    }

    fun BuildDialog(v: View) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.Theme_AppCompat_Dialog))
        val inflater = this.layoutInflater
        builder.setView(inflater.inflate(R.layout.alert_search, null))
            .setPositiveButton("Искать", ::onPositiveButtonClick)
            .setNegativeButton("") { diag: DialogInterface, id: Int -> diag.cancel() }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun onPositiveButtonClick(dialog: DialogInterface, which: Int) {
        val diag = dialog as Dialog

        val from = diag.fromSearch.text.toString()
        val to = diag.toSearch.text.toString()

        val query = CaseQuery(
            diag.numberSearch.text.toString(),
            if (from.isBlank()) 0 else from.toLong(),
            if (to.isBlank()) 0 else to.toLong(),
            diag.sideSearch.text.toString(),
            0,
            10
        )
        model.requestCases(query, userId)
        diag.cancel()
    }

    private fun onExploringClick(case: ShortCase) {
        val intent = Intent(this, CaseViewActivity::class.java)
        val bundle = intent.extras

        runBlocking {
            val bigCase = case.bigCaseJob!!.await()
            bundle?.putInt(CaseViewActivity.USER_ID, userId!!)
            bundle?.putString(CaseViewActivity.CASE_TYPE, bigCase?.caseType)
            bundle?.putParcelable(CaseViewActivity.DATA, case)
            bigCase?.events?.pushToBundle(bundle, CaseViewActivity.EVENTS)
            bigCase?.mainData?.pushToBundle(bundle, CaseViewActivity.MAINDATA)
            bigCase?.sides?.pushToBundle(bundle, CaseViewActivity.SIDES)
        }

        startActivity(intent)
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
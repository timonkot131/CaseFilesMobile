package com.example.casefilesmobile

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.casefilesmobile.adapters.ExploringRecyclerAdapter
import com.example.casefilesmobile.pojo.*
import com.example.casefilesmobile.viewmodels.CodesViewModel
import com.example.casefilesmobile.viewmodels.ExploringCasesViewModel
import kotlinx.android.synthetic.main.activity_exploring_cases.*
import kotlinx.android.synthetic.main.alert_search.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class ExploringCasesActivity : AppCompatActivity() {

    private val model: ExploringCasesViewModel by viewModels()
    private val codes: CodesViewModel by viewModels()
    private var adapter: ExploringRecyclerAdapter? = null

    private var regSpinner: Spinner? = null
    private var courtSpinner: Spinner? = null

    private var userId: Int? = null

    private var scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exploring_cases)
        setSupportActionBar(exploringBottomBar)

        model.cases.observe(this, ::onCasesObserve)
        codes.courts.observe(this, ::onCourtsObserve)
        codes.regions.observe(this, ::onRegionsObserve)

        userId = intent.extras?.getInt(CaseViewActivity.USER_ID)

        exploringRecycler.layoutManager = LinearLayoutManager(applicationContext)

        exploringFab.setOnClickListener(::buildDialog)

        model.requestCases()

        exploringBottomBar.setNavigationIcon(R.drawable.ic_baseline_exit_to_app_24)
        exploringBottomBar.setNavigationOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.exploring_bottom_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_bottomTracking -> {
                val intent = Intent(this, TrackingActivity::class.java)
                intent.putExtra(TrackingActivity.USER_ID, userId)
                startActivity(intent)
            }
            else -> {
                when(item.title) { //item returns fine title, but strange id
                    "next" -> model.nextPage()
                    "prev" -> model.previousPage()
                }
            }
        }

        return true
    }

    private fun buildDialog(v: View) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.alertDialogStyle))
        val inflater = this.layoutInflater
        builder.setView(inflater.inflate(R.layout.alert_search, null))
            .setPositiveButton(getString(R.string.Search), ::onPositiveButtonClick)
            .setNegativeButton(getString(R.string.Cancel)) { diag: DialogInterface, id: Int ->
                regSpinner = null
                courtSpinner = null
                diag.cancel()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()

        regSpinner = dialog.regSpinner
        courtSpinner = dialog.courtSpinner
        val current = Calendar.getInstance()
        dialog.fromSearch.updateDate(
            current.get(Calendar.YEAR),
            current.get(Calendar.MONTH),
            current.get(Calendar.DAY_OF_MONTH)
        )
        codes.getRegions()

        regSpinner!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
                    val region = p0.selectedItem as RegionCode
                    codes.getCourts(region)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    courtSpinner?.adapter = ArrayAdapter(
                        this@ExploringCasesActivity,
                        R.layout.support_simple_spinner_dropdown_item,
                        listOf<CourtCode>()
                    )
                }
            }
    }

    private fun onPositiveButtonClick(dialog: DialogInterface, which: Int) {
        val diag = dialog as Dialog

        exploringProgressBar.isIndeterminate = true

        val selectedRegionCode: RegionCode? = diag.regSpinner.selectedItem as RegionCode

        if (selectedRegionCode == null) {
            Toast.makeText(this, getString(R.string.PleaseChoseRegion), Toast.LENGTH_LONG).show()
            return
        }

        val selectedCourtCode: CourtCode? = diag.courtSpinner.selectedItem as CourtCode

        val from = diag.fromSearch.epochTicks // why fine? standard epoch ticks returns nonsense
        val to = diag.toSearch.epochTicks

        val query = CaseQuery(
            selectedRegionCode.code,
            selectedCourtCode?.code ?: "",
            diag.numberSearch.text.toString(),
            from,
            to,
            diag.sideSearch.text.toString(),
            0
        )
        model.requestCases(query, userId)
        diag.cancel()
    }

    private fun awaitBigCaseJob(job: Deferred<BigCase?>, onComplete: (BigCase?) -> Unit) {
        exploringProgressBar.isIndeterminate = true
        scope.launch(Dispatchers.IO) {
            val bigCase = job.await()
            runOnUiThread {
                onComplete(bigCase)
            }
        }
    }

    private fun onExploringClick(case: ShortCase) {
        val intent = Intent(this, CaseViewActivity::class.java)

        awaitBigCaseJob(case.bigCaseJob!!()) {
            exploringProgressBar.isIndeterminate = false
            intent.putExtra(CaseViewActivity.USER_ID, userId!!)
            intent.putExtra(CaseViewActivity.CASE_TYPE, it?.caseType)
            intent.putExtra(CaseViewActivity.SHORT_CASE, case)
            it?.events?.pushToBundle(intent, CaseViewActivity.EVENTS)
            it?.mainData?.pushToBundle(intent, CaseViewActivity.MAINDATA)
            it?.sides?.pushToBundle(intent, CaseViewActivity.SIDES)
            startActivity(intent)
        }
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

    private fun onCasesObserve(response: ShortCaseResponse) {
        exploringProgressBar.isIndeterminate = false

        when (response.code) {
            200 -> updateCases(response.cases)
            else -> showMessage(getString(R.string.CantFindCases))
        }
    }

    private fun onCourtsObserve(list: List<CourtCode>) {
        courtSpinner?.let {
            it.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)
        }
    }

    private fun onRegionsObserve(list: List<RegionCode>) {
        regSpinner?.let {
            it.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)
        }
    }
}
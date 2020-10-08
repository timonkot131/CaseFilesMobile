package com.example.casefilesmobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.casefilesmobile.R
import com.example.casefilesmobile.pojo.TrackingCase
import kotlinx.android.synthetic.main.trackingcase.view.*
import java.text.SimpleDateFormat

class TrackingRecyclerAdapter (
    private val context: Context,
    private val caseArray: MutableList<TrackingCase>,
    private val clickListener: (TrackingCase) -> Unit
) : RecyclerView.Adapter<TrackingRecyclerAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(context).inflate(R.layout.trackingcase, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(caseArray[position]) {
        holder.court.text = court
        holder.number.text = number
        holder.decision.text = decision

        val date = SimpleDateFormat(context.getString(R.string.date_formatting)).format(registrDate)
        val time = SimpleDateFormat(context.getString(R.string.time_formatting)).format(registrDate)

        holder.date.text = context.getString(R.string.date_and_time, date, time)

        holder.itemView.setOnClickListener {
            clickListener(this)
        }
    }

    fun clear() {
        caseArray.clear()
        notifyDataSetChanged()
    }

    fun update(cases: List<TrackingCase>){
        caseArray.clear()
        caseArray.addAll(cases)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = caseArray.size

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v){
        var number: TextView = v.trackNumber
        var date: TextView = v.trackDate
        var court: TextView = v.trackCourt
        var decision: TextView = v.trackDecision
    }
}
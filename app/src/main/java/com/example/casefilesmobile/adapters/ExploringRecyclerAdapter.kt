package com.example.casefilesmobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.casefilesmobile.R
import com.example.casefilesmobile.pojo.ShortCase
import kotlinx.android.synthetic.main.shortcase.view.*
import java.text.SimpleDateFormat

class ExploringRecyclerAdapter(
    private val context: Context,
    private val caseArray: MutableList<ShortCase>,
    private val clickListener: (ShortCase) -> Unit
) : RecyclerView.Adapter<ExploringRecyclerAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(context).inflate(R.layout.shortcase, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(caseArray[position]) {
        holder.court.text = court
        holder.number.text = number

        val date = SimpleDateFormat(context.getString(R.string.date_formatting)).format(registrationDate)
        val time = SimpleDateFormat(context.getString(R.string.time_formatting)).format(registrationDate)

        holder.date.text = context.getString(R.string.date_and_time, date, time)

        holder.itemView.setOnClickListener {
            clickListener(this)
        }
    }

    fun clear() {
        caseArray.clear()
        notifyDataSetChanged()
    }

    fun update(cases: List<ShortCase>){
        caseArray.clear()
        caseArray.addAll(cases)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = caseArray.size

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v){
        var number: TextView = v.shortNumber
        var date: TextView = v.shortDate
        var court: TextView = v.shortCourt
    }
}
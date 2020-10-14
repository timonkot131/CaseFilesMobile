package com.example.casefilesmobile.pojo

import com.example.casefilesmobile.toList
import org.json.JSONArray
import org.json.JSONObject

data class BigCase(
    val caseType: String,
    val mainData: Table?,
    val sides: Table?,
    val events: Table?
) {
    fun getJson() : JSONObject{
        val body = JSONObject()
        body.put("caseType", caseType)

        val mainDataTable = mainData?.let { table ->
            val obj = JSONObject()
            table.body.forEach {
                obj.put(it.first(),it.last())
            }
            obj
        }

        val sides = sides?.let { table ->
            val array = JSONArray()
            table.body.forEachIndexed { index, list ->
                val obj = JSONObject()
                obj.put(table.headers[index], list[index])
                array.put(obj)
            }
            array
        }

        val events = events?.let {  table ->
            val array = JSONArray()
            table.body.forEachIndexed { index, list ->
                val obj = JSONObject()
                obj.put(table.headers[index], list[index])
                array.put(obj)
            }
            array
        }

        body.put("mainData", mainDataTable)
        body.put("sides", sides)
        body.put("events", events)

        return body
    }

    companion object{

        fun parseCommonTable(array: JSONArray): Table {
            val headers = array.toList()
                .first()
                .keys()
                .asSequence()
                .toList()

            val body =
                array
                    .toList()
                    .toTypedArray()
                    .map { it to it.keys().asSequence() }
                    .map { pair ->
                        pair.second.map { pair.first.getString(it) }.toList()
                    }

            return Table(headers, body)
        }

        fun parseJson(text: String): BigCase {
            val body = JSONObject(text)
            val type = body.getString("caseType")

            val mainDataJson: JSONObject? =
                if(!body.isNull("mainData"))
                    body.getJSONObject("mainData")
                else null

            val sides: JSONArray? =
                if(!body.isNull("sides"))
                    body.getJSONArray("sides")
                else null

            val events: JSONArray? =
                if(!body.isNull("events"))
                    body.getJSONArray("events")
                else null

            val mainData =
                mainDataJson?.keys()?.asSequence()?.map {
                    val data = mainDataJson.getString(it)
                    it to data
                }?.toList()
                    ?.map { listOf(it.first, it.second) }


            var mainDataTable: Table? = mainData?.let {
                Table(listOf("",""), it)
            }

            val eventsDataTable: Table? = events?.let{
                parseCommonTable(it)
            }

            val sidesDataTable: Table? = sides?.let{
                parseCommonTable(it)
            }

            return BigCase(type, mainDataTable, eventsDataTable, sidesDataTable)
        }
    }

}

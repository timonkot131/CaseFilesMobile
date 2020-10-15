package com.example.casefilesmobile.network_operations

import cz.msebera.android.httpclient.client.utils.URIBuilder
import java.net.URI

class TrackingCases {

    companion object{
        fun getUri(userId: Int, page: Int, size: Int?): URI {
            val builder = URIBuilder()
                .setHost("http://10.0.3.2:44370/api/cases/trackedCases/" + userId)
                .addParameter("page", page.toString())

            size?.let{
                builder.addParameter("size", it.toString())
            }

            return builder.build()
        }
    }
}
package com.example.casefilesmobile.pojo

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Deferred
import java.util.*

data class Account(
    val id: Int,
    val login: String,
    val pwd: String,
    val age: Int,
    val registrDate: Long,
    val firstName: String,
    val secondName: String
)


@Parcelize
data class ShortCase(
    val id: String,
    val judge: String,
    val number: String,
    val registrationDate: Date,
    val court: String,
    val region: String
) : Parcelable
{
    @IgnoredOnParcel
    var bigCaseJob: (() -> Deferred<BigCase?>)? = null
}

@Parcelize
data class TrackingCase(
    val id: Int,
    val registrDate: Long,
    val court: String,
    val reg: String,
    val number: String,
    val json: String
) : Parcelable

data class RegionCode(
    val region: String,
    val code: String
) {
    override fun toString() = region
}

data class CourtCode(
    val court: String,
    val code: String
) {
    override fun toString() = court
}

data class CaseQuery(
    val region: String,
    val court: String,
    val number: String,
    val from: Long,
    val to: Long,
    val side: String,
    val page: Int
)

data class AccountResponse(val account: Account?, val code: Int)

data class BigCaseResponse(val cases: List<BigCase>, val code: Int)

data class ShortCaseResponse(val cases: List<ShortCase>, val code: Int)

data class TrackingResponse(val cases: List<TrackingCase>, val code: Int)
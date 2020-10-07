package com.example.casefilesmobile.pojo

data class Account(
    val id: Int,
    val login: String,
    val pwd: String,
    val age: Int,
    val registrDate: Long,
    val firstName: String,
    val secondName: String
)

data class BigCase(
    val caseType: String,
    val mainData: HashMap<String, String>,
    val sides: Array<HashMap<String, String>>,
    val events: Array<HashMap<String, String>>
)

data class ShortCase(
    val id: String,
    val judge: String,
    val number: String,
    val
    val sides: Array<HashMap<String, String>>,
    val events: Array<HashMap<String, String>>
)

data class TrackingCase(
    val id: Int,
    val decision: String,
    val registrDate: Long,
    val court: String,
    val number: String,
    val json: String
)

data class CaseQuery(
    val number: String,
    val from: Long,
    val to: Long,
    val side: String,
    val page: Int,
    val pageSize: Int
)

data class AccountResponse(val account: Account?, val code: Int)

data class BigCaseResponse(val cases: List<BigCase>, val code: Int)

data class TrackingResponse(val cases: List<TrackingCase>, val code: Int)
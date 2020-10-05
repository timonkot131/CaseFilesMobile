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

data class AccountResponse(val account: Account?, val code: Int)
package com.example.teamatch.data

import com.google.firebase.Timestamp

data class Match(
    val id: String = "",
    val district: String = "",
    val pitchName: String = "",
    val date: String = Timestamp.now().toString(),
    val startTime: String = "",
    val endTime: String = "",
    val teamSize: String = "",
    val creatorId: String = ""
)



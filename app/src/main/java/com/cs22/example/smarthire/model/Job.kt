package com.cs22.example.smarthire.model

data class Job(
    val id: String? = null,
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val match_percentage: Double = 0.0,
    val required_skills: List<String> = emptyList(),
    val description: String = ""
)

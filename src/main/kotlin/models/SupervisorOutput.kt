package com.research.models

/**
 * Output from supervisor agent
 */
data class SupervisorOutput(
    val notes: List<String>,
    val finalThinking: String
)
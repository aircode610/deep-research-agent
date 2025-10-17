package com.research.models

/**
 * Result from the research workflow
 */
data class ResearchResult(
    val researchBrief: String,
    val findings: List<String>,
    val supervisorThinking: String
)
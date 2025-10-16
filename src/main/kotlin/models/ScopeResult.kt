package com.research.models

/**
 * Result of the scoping workflow containing the research brief and conversation history
 */
data class ScopeResult(
    val researchBrief: String,
    val conversationHistory: List<String>,
    val verification: String
)
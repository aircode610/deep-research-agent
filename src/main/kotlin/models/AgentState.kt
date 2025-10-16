package com.research.models

import ai.koog.prompt.message.Message

/**
 * Simple state for clarification workflow
 */
data class AgentState(
    val messages: List<Message> = emptyList(),
    val researchBrief: String? = null
)
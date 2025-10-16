package com.research.models.state

import ai.koog.prompt.message.Message

/**
 * Main state for the clarification and brief generation workflow
 */
data class AgentState(
    val messages: List<Message> = emptyList(),
    val researchBrief: String? = null,
    val needsClarification: Boolean = true
)

/**
 * Input state - only contains initial user message
 */
data class AgentInputState(
    val userQuery: String
)
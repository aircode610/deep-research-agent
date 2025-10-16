package com.research.models

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Structured output for clarification decision
 */
@Serializable
@SerialName("ClarificationResponse")
@LLMDescription("Decision on whether clarification is needed from the user")
data class ClarificationResponse(
    @property:LLMDescription("Whether the user needs to be asked a clarifying question")
    val needClarification: Boolean,

    @property:LLMDescription("A question to ask the user to clarify the report scope (empty if no clarification needed)")
    val question: String = "",

    @property:LLMDescription("Verification message that we will start research (empty if clarification needed)")
    val verification: String = ""
)
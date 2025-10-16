package com.research.models.schemas

import kotlinx.serialization.Serializable

/**
 * Structured output schema for clarification decision
 * Corresponds to ClarifyWithUser in Python version
 */
@Serializable
data class ClarificationResponse(
    val needClarification: Boolean,
    val question: String = "",
    val verification: String = ""
) {
    companion object {
        /**
         * Creates a response that needs clarification
         */
        fun needsClarification(question: String) = ClarificationResponse(
            needClarification = true,
            question = question,
            verification = ""
        )

        /**
         * Creates a response that's ready to proceed
         */
        fun readyToProceed(verification: String) = ClarificationResponse(
            needClarification = false,
            question = "",
            verification = verification
        )
    }
}

/**
 * Structured output schema for research brief generation
 * Corresponds to ResearchQuestion in Python version
 */
@Serializable
data class ResearchBrief(
    val researchBrief: String
)
package com.research.models

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Structured output for research brief
 */
@Serializable
@SerialName("ResearchBrief")
@LLMDescription("A detailed research question that will guide the research")
data class ResearchBriefSchema(
    @property:LLMDescription("A detailed and specific research question")
    val researchBrief: String
)
package com.research.models

/**
 * Output from researcher agent
 */
data class ResearcherOutput(
    val compressedResearch: String,
    val rawNotes: List<String>
)
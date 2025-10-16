package com.research.workflows

import com.research.agents.BriefGenerationAgent
import com.research.agents.ClarificationAgent
import com.research.models.ScopeResult

/**
 * Scoping workflow that orchestrates clarification and brief generation agents
 * This workflow encapsulates the entire scoping phase by composing existing agents
 */
class ScopeWorkflow(apiKey: String) {

    private val clarificationAgent = ClarificationAgent(apiKey)
    private val briefGenerationAgent = BriefGenerationAgent(apiKey)

    /**
     * Execute the complete scoping workflow:
     * 1. Clarification phase - iterative clarification until ready
     * 2. Brief generation phase - generate detailed research brief
     */
    suspend fun executeScopeWorkflow(initialQuery: String): ScopeResult {
        val conversationHistory = mutableListOf<String>()
        conversationHistory.add("User: $initialQuery")

        // Phase 1: Clarification using ClarificationAgent
        println("\n=== Phase 1: Clarification ===")
        val verification = runClarificationPhase(conversationHistory)

        // Phase 2: Brief Generation using BriefGenerationAgent
        println("\n=== Phase 2: Brief Generation ===")
        val researchBrief = runBriefGenerationPhase(conversationHistory)

        return ScopeResult(
            researchBrief = researchBrief,
            conversationHistory = conversationHistory,
            verification = verification
        )
    }

    /**
     * Clarification phase - iterative questioning using ClarificationAgent
     */
    private suspend fun runClarificationPhase(conversationHistory: MutableList<String>): String {
        while (true) {
            println("--- Checking if clarification needed ---")

            val clarificationResponse = clarificationAgent.checkClarification(conversationHistory)

            if (!clarificationResponse.needClarification) {
                println("✓ Ready to proceed!")
                println("Verification: ${clarificationResponse.verification}")
                return clarificationResponse.verification
            }

            // Need clarification
            println("\nAssistant: ${clarificationResponse.question}")
            conversationHistory.add("Assistant: ${clarificationResponse.question}")

            // Get user response
            print("\nYou: ")
            val userResponse = readlnOrNull() ?: ""
            conversationHistory.add("User: $userResponse")
        }
    }

    /**
     * Brief generation phase - generate detailed research brief using BriefGenerationAgent
     */
    private suspend fun runBriefGenerationPhase(conversationHistory: List<String>): String {
        println("--- Generating research brief ---")

        val researchBrief = briefGenerationAgent.generateBrief(conversationHistory)

        println("✓ Research brief generated:")
        println(researchBrief)

        return researchBrief
    }
}
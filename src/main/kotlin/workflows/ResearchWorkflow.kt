package com.research.workflows

import com.research.agents.ResearcherAgent
import com.research.agents.SupervisorAgent
import com.research.models.ResearchResult

/**
 * Research workflow that orchestrates supervisor and researcher agents.
 * This workflow encapsulates the entire research coordination phase.
 */
class ResearchWorkflow(
    apiKey: String,
    tavilyApiKey: String,
    maxConcurrentResearchers: Int = 2,
    maxIterations: Int = 6
) {
    private val researcherAgent = ResearcherAgent(
        apiKey = apiKey,
        tavilyApiKey = tavilyApiKey
    )

    private val supervisorAgent = SupervisorAgent(
        apiKey = apiKey,
        researcherAgent = researcherAgent,
        maxConcurrentResearchers = maxConcurrentResearchers,
        maxIterations = maxIterations
    )

    /**
     * Execute the complete research workflow:
     * 1. Supervisor analyzes the research brief
     * 2. Supervisor delegates to specialized researcher agents
     * 3. Researchers conduct web searches and gather information
     * 4. Supervisor aggregates all findings
     */
    suspend fun executeResearch(researchBrief: String): ResearchResult {
        println("\n" + "=".repeat(80))
        println("=== RESEARCH PHASE ===")
        println("=".repeat(80))
        println("Brief: ${researchBrief.take(100)}...")
        println("=".repeat(80) + "\n")

        val supervisorOutput = supervisorAgent.research(researchBrief)

        println("\n" + "=".repeat(80))
        println("RESEARCH PHASE COMPLETE")
        println("Total findings collected: ${supervisorOutput.notes.size}")
        println("=".repeat(80) + "\n")

        return ResearchResult(
            researchBrief = researchBrief,
            findings = supervisorOutput.notes,
            supervisorThinking = supervisorOutput.finalThinking
        )
    }
}
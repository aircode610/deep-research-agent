package com.research.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.research.models.SupervisorOutput
import com.research.prompts.SupervisorPrompts
import com.research.tools.ConductMultipleResearchTool
import com.research.tools.ConductResearchTool
import com.research.tools.ResearchCompleteTool
import com.research.tools.ThinkTool

/**
 * Supervisor agent that coordinates research across multiple specialized researcher agents.
 *
 * The supervisor:
 * 1. Analyzes the research brief
 * 2. Breaks it down into focused subtopics
 * 3. Delegates research to sub-agents (can run in parallel)
 * 4. Aggregates compressed findings
 * 5. Decides when research is complete
 */
class SupervisorAgent(
    private val apiKey: String,
    private val researcherAgent: ResearcherAgent,
    private val maxConcurrentResearchers: Int = 3,
    private val maxIterations: Int = 6
) {
    private val promptExecutor = simpleOpenAIExecutor(apiKey)

    suspend fun research(researchBrief: String): SupervisorOutput {
        val compressedFindings = mutableListOf<String>()

        try {
            // Single research tool
            val conductResearch = ConductResearchTool(researcherAgent) { finding ->
                compressedFindings.add(finding)
            }

            // Batch parallel research tool
            val conductMultipleResearch = ConductMultipleResearchTool(researcherAgent) { findings ->
                compressedFindings.addAll(findings)
            }

            val toolRegistry = ToolRegistry {
                tool(conductResearch)
                tool(conductMultipleResearch) // Add batch tool
                tool(ResearchCompleteTool)
                tool(ThinkTool())
            }

            // Configure supervisor agent
            val agentConfig = AIAgentConfig(
                prompt = prompt("supervisor") {
                    system(
                        SupervisorPrompts.createSupervisorPrompt(
                            maxConcurrentResearchers = maxConcurrentResearchers,
                            maxIterations = maxIterations
                        )
                    )
                },
                model = OpenAIModels.Chat.GPT4o,
                maxAgentIterations = maxIterations
            )

            // Create and run supervisor agent
            val agent = AIAgent(
                promptExecutor = promptExecutor,
                agentConfig = agentConfig,
                toolRegistry = toolRegistry
            )

            // Run supervisor with the research brief
            val finalThinking = agent.run(researchBrief)

            println("\n" + "=".repeat(80))
            println("SUPERVISOR: Research coordination complete")
            println("Total findings collected: ${compressedFindings.size}")
            println("=".repeat(80) + "\n")

            return SupervisorOutput(
                notes = compressedFindings,
                finalThinking = finalThinking
            )

        } catch (e: Exception) {
            println("Error in supervisor: ${e.message}")
            throw e
        }
    }
}
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
 */
class SupervisorAgent(
    apiKey: String,
    private val researcherAgent: ResearcherAgent,
    private val maxConcurrentResearchers: Int = 3,
    private val maxIterations: Int = 6
) {
    private val promptExecutor = simpleOpenAIExecutor(apiKey)

    suspend fun research(researchBrief: String): SupervisorOutput {
        val compressedFindings = mutableListOf<String>()

        try {
            val conductResearch = ConductResearchTool(researcherAgent) { finding ->
                compressedFindings.add(finding)
            }

            val conductMultipleResearch = ConductMultipleResearchTool(researcherAgent) { findings ->
                compressedFindings.addAll(findings)
            }

            val toolRegistry = ToolRegistry {
                tool(conductResearch)
                tool(conductMultipleResearch)
                tool(ResearchCompleteTool)
                tool(ThinkTool())
            }

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

            val agent = AIAgent(
                promptExecutor = promptExecutor,
                agentConfig = agentConfig,
                toolRegistry = toolRegistry
            )

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
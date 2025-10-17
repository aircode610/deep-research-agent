package com.research.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.research.prompts.FinalReportPrompts

/**
 * Agent responsible for synthesizing research findings into a comprehensive final report.
 */
class FinalReportAgent(
    private val apiKey: String
) {
    private val promptExecutor = simpleOpenAIExecutor(apiKey)

    /**
     * Generate final report from research brief and collected findings.
     *
     * @param researchBrief The original research question/brief
     * @param findings List of compressed research findings from sub-agents
     * @return Formatted markdown report with sources
     */
    suspend fun generateReport(
        researchBrief: String,
        findings: List<String>
    ): String {
        println("\n" + "=".repeat(80))
        println("📝 FINAL REPORT: Synthesizing ${findings.size} research findings")
        println("=".repeat(80) + "\n")

        try {
            val combinedFindings = findings.joinToString("\n\n" + "=".repeat(80) + "\n\n")

            val agentConfig = AIAgentConfig(
                prompt = prompt("report-writer") {
                    system(FinalReportPrompts.createFinalReportPrompt())
                    user(
                        FinalReportPrompts.createFinalReportUserMessage(
                            researchBrief = researchBrief,
                            findings = combinedFindings
                        )
                    )
                },
                model = OpenAIModels.Chat.GPT4o, // Use GPT-4 for quality reports
                maxAgentIterations = 3 // Single-shot generation
            )

            val agent = AIAgent(
                promptExecutor = promptExecutor,
                agentConfig = agentConfig,
                toolRegistry = ToolRegistry.EMPTY // No tools needed
            )

            val report = agent.run("")

            println("\n" + "=".repeat(80))
            println("✅ FINAL REPORT: Generation complete")
            println("=".repeat(80) + "\n")

            return report

        } catch (e: Exception) {
            println("⚠️ Error generating final report: ${e.message}")
            throw e
        }
    }
}
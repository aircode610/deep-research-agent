package com.research.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.research.models.ResearcherOutput
import com.research.prompts.ResearchPrompts
import com.research.tools.TavilySearchTool
import com.research.tools.ThinkTool

/**
 * Individual researcher agent that conducts focused research on a specific topic
 */
class ResearcherAgent(
    apiKey: String,
    private val tavilyApiKey: String
) {
    private val promptExecutor = simpleOpenAIExecutor(apiKey)

    /**
     * Execute research on a given topic
     */
    suspend fun research(researchTopic: String): ResearcherOutput {
        println("\n" + "=".repeat(80))
        println("Starting Research: $researchTopic")
        println("=".repeat(80) + "\n")

        val searchQueries = mutableListOf<String>()
        val searchResults = mutableListOf<String>()

        val researchResult = conductResearch(researchTopic, searchQueries, searchResults)
        val compressedResearch = compressFindings(researchTopic, researchResult, searchQueries, searchResults)

        return ResearcherOutput(
            compressedResearch = compressedResearch,
            rawNotes = searchResults
        )
    }

    /**
     * Phase 1: Conduct iterative research using search and think tools
     */
    private suspend fun conductResearch(
        researchTopic: String,
        searchQueries: MutableList<String>,
        searchResults: MutableList<String>
    ): String {
        // Create Tavily tool with callback to capture results
        val tavilyTool = TavilySearchTool(
            apiKey = tavilyApiKey,
            onSearchExecuted = { query, result ->
                searchQueries.add(query)
                searchResults.add(result)
            }
        )
        val thinkTool = ThinkTool()

        try {
            val toolRegistry = ToolRegistry {
                tool(tavilyTool)
                tool(thinkTool)
            }

            val agentConfig = AIAgentConfig(
                prompt = prompt("researcher") {
                    system(ResearchPrompts.createResearcherPrompt())
                },
                model = OpenAIModels.Chat.GPT4o,
                maxAgentIterations = 15
            )

            val agent = AIAgent(
                promptExecutor = promptExecutor,
                agentConfig = agentConfig,
                toolRegistry = toolRegistry
            )

            val result = agent.run(researchTopic)

            println("\nResearch Complete")
            println("Queries executed: ${searchQueries.size}")
            println("Agent found information and is ready for compression\n")

            return result

        } catch (e: Exception) {
            println("Error during research: ${e.message}")
            throw e
        } finally {
            tavilyTool.close()
        }
    }

    /**
     * Phase 2: Compress findings using AIAgent
     */
    private suspend fun compressFindings(
        researchTopic: String,
        researchResult: String,
        searchQueries: List<String>,
        searchResults: List<String>
    ): String {
        println("Compressing research findings...")

        try {
            val fullContext = buildString {
                appendLine("RESEARCH TOPIC: $researchTopic")
                appendLine("\n" + "=".repeat(80))
                appendLine("\nSEARCH QUERIES EXECUTED:")
                searchQueries.forEachIndexed { index, query ->
                    appendLine("${index + 1}. $query")
                }

                appendLine("\n" + "=".repeat(80))
                appendLine("\nRAW SEARCH RESULTS WITH SOURCES:")
                searchResults.forEachIndexed { index, result ->
                    appendLine("\n--- SEARCH ${index + 1} RESULTS ---")
                    appendLine(result)
                }

                appendLine("\n" + "=".repeat(80))
                appendLine("\nRESEARCHER'S FINAL ANALYSIS:")
                appendLine(researchResult)
            }

            val compressionAgent = AIAgent(
                promptExecutor = promptExecutor,
                toolRegistry = ToolRegistry.EMPTY,
                agentConfig = AIAgentConfig(
                    prompt = prompt("compression") {
                        system(ResearchPrompts.createCompressionPrompt())
                    },
                    model = OpenAIModels.Chat.GPT5Nano,
                    maxAgentIterations = 5
                )
            )

            val compressed = compressionAgent.run(fullContext)

            println("Compression complete\n")

            return compressed

        } catch (e: Exception) {
            println("⚠Compression failed: ${e.message}")
            println("Returning uncompressed research result")
            return "# Research Findings (Uncompressed)\n\n$researchResult"
        }
    }
}
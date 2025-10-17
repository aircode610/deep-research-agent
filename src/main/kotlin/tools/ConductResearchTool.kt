package com.research.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import com.research.agents.ResearcherAgent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable

/**
 * Tool for delegating research - handles parallel execution internally
 */
class ConductResearchTool(
    private val researcherAgent: ResearcherAgent,
    private val onResearchComplete: (String) -> Unit = {}
) : SimpleTool<ConductResearchTool.Args>() {

    @Serializable
    data class Args(
        @property:LLMDescription("The research topic to investigate...")
        val researchTopic: String
    )

    override val argsSerializer = Args.serializer()
    override val description = """..."""

    override suspend fun doExecute(args: Args): String {
        println("\n" + "=".repeat(80))
        println("🔍 DELEGATING RESEARCH TO SUB-AGENT")
        println("Topic: ${args.researchTopic.take(100)}...")
        println("=".repeat(80))

        return try {
            val result = researcherAgent.research(args.researchTopic)

            println("✅ Sub-agent completed research")
            println("=".repeat(80) + "\n")

            onResearchComplete(result.compressedResearch)
            result.compressedResearch

        } catch (e: Exception) {
            val errorMsg = "⚠️ Error during delegated research: ${e.message}"
            println(errorMsg)
            println("=".repeat(80) + "\n")
            errorMsg
        }
    }
}

/**
 * Batch version for parallel execution
 */
class ConductMultipleResearchTool(
    private val researcherAgent: ResearcherAgent,
    private val onResearchComplete: (List<String>) -> Unit = {}
) : SimpleTool<ConductMultipleResearchTool.Args>() {

    @Serializable
    data class Args(
        @property:LLMDescription("List of research topics to investigate in parallel (max 3)")
        val researchTopics: List<String>
    )

    override val argsSerializer = Args.serializer()

    override val description = """
        Delegate multiple research tasks to specialized sub-agents that run IN PARALLEL.
        
        Use this when you have multiple INDEPENDENT subtopics that can be researched simultaneously.
        This is much faster than making sequential ConductResearch calls.
        
        Maximum 3 parallel research tasks per call.
    """.trimIndent()

    override suspend fun doExecute(args: Args): String = coroutineScope {
        val topics = args.researchTopics.take(3) // Enforce max

        println("\n" + "=".repeat(80))
        println("🔍 LAUNCHING ${topics.size} PARALLEL RESEARCH AGENTS")
        topics.forEachIndexed { index, topic ->
            println("${index + 1}. ${topic.take(80)}...")
        }
        println("=".repeat(80))

        try {
            // Launch all research tasks in parallel
            val results = topics.map { topic ->
                async {
                    println("▶️  Starting research on: ${topic.take(50)}...")
                    researcherAgent.research(topic)
                }
            }.awaitAll()

            println("\n✅ All ${results.size} parallel research tasks completed")
            println("=".repeat(80) + "\n")

            val compressed = results.map { it.compressedResearch }
            onResearchComplete(compressed)

            // Return combined results
            compressed.mapIndexed { index, finding ->
                "## Finding ${index + 1}: ${topics[index].take(50)}...\n\n$finding"
            }.joinToString("\n\n" + "=".repeat(80) + "\n\n")

        } catch (e: Exception) {
            val errorMsg = "⚠️ Error during parallel research: ${e.message}"
            println(errorMsg)
            println("=".repeat(80) + "\n")
            errorMsg
        }
    }
}
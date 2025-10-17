package com.research.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import com.research.agents.ResearcherAgent
import kotlinx.serialization.Serializable

/**
 * Tool for delegating research tasks to specialized sub-agents.
 */
class ConductResearchTool(
    private val researcherAgent: ResearcherAgent
) : SimpleTool<ConductResearchTool.Args>() {

    @Serializable
    data class Args(
        @property:LLMDescription(
            """The research topic to investigate. 
            
            CRITICAL INSTRUCTIONS:
            - Provide COMPLETE, STANDALONE instructions for the researcher
            - The sub-agent CANNOT see other agents' work or previous context
            - Include ALL necessary context and requirements in this topic description
            - Be VERY specific and detailed (at least a paragraph)
            - Do NOT use acronyms or abbreviations without explanation
            - Specify what information should be prioritized or focused on
            
            Example BAD topic: "Research AI safety at OpenAI"
            Example GOOD topic: "Research OpenAI's approach to AI safety, focusing on: 
            1) Their key safety techniques and methodologies, 
            2) Published papers and research on alignment, 
            3) Specific safety teams and their responsibilities, 
            4) Any public incidents or lessons learned. 
            Prioritize official OpenAI sources and recent developments from 2024-2025."
            """
        )
        val researchTopic: String
    )

    override val argsSerializer = Args.serializer()

    override val description = """
        Delegate a research task to a specialized sub-agent.
        
        Use this tool to:
        - Break down complex research into focused subtopics
        - Conduct parallel research on independent topics
        - Investigate specific aspects that need deep dive
        
        The sub-agent will:
        1. Conduct web searches on the topic
        2. Gather and analyze information
        3. Return compressed research findings
        
        IMPORTANT: Provide complete, standalone instructions. 
        The sub-agent has NO access to:
        - Other agents' findings
        - Previous conversation history
        - The overall research context
        
        Each research topic should be self-contained and detailed.
    """.trimIndent()

    override suspend fun doExecute(args: Args): String {
        println("\n" + "=".repeat(80))
        println("DELEGATING RESEARCH TO SUB-AGENT")
        println("Topic: ${args.researchTopic.take(100)}...")
        println("=".repeat(80))

        return try {
            val result = researcherAgent.research(args.researchTopic)

            println("Sub-agent completed research")
            println("Compressed findings ready")
            println("=".repeat(80) + "\n")

            result.compressedResearch

        } catch (e: Exception) {
            val errorMsg = "Error during delegated research: ${e.message}"
            println(errorMsg)
            println("=".repeat(80) + "\n")
            errorMsg
        }
    }
}
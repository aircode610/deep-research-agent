package com.research.tools

import ai.koog.agents.core.tools.SimpleTool
import kotlinx.serialization.Serializable

/**
 * Tool to signal that research is complete.
 */
object ResearchCompleteTool : SimpleTool<ResearchCompleteTool.Args>() {

    @Serializable
    data class Args(
        val unused: String = ""
    )

    override val argsSerializer = Args.serializer()

    override val description = """
        Signal that research is complete and you have sufficient information.
        
        Call this tool when:
        - You have gathered comprehensive information to answer the research brief
        - All critical aspects of the question have been investigated
        - You have findings from all necessary research sub-agents
        - Additional research would not significantly improve the answer
        
        DO NOT call this if:
        - Key information is still missing
        - The research brief has unanswered components
        - More investigation would significantly improve quality
        
        Once called, the research phase ends and report generation begins.
    """.trimIndent()

    override suspend fun doExecute(args: Args): String {
        println("\n" + "=".repeat(80))
        println("RESEARCH COMPLETE")
        println("Supervisor has determined sufficient information gathered")
        println("=".repeat(80) + "\n")

        return "Research marked as complete. Proceeding to final report generation."
    }
}
package com.research.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

/**
 * Tool for strategic reflection on research progress and decision-making.
 */
class ThinkTool : SimpleTool<ThinkTool.Args>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Your detailed reflection on research progress, findings, gaps, and next steps")
        val reflection: String
    )

    override val argsSerializer = Args.serializer()

    override val description = """
        Tool for strategic reflection on research progress and decision-making.
        
        Use this tool after each search to analyze results and plan next steps systematically.
        This creates a deliberate pause in the research workflow for quality decision-making.
        
        When to use:
        - After receiving search results: What key information did I find?
        - Before deciding next steps: Do I have enough to answer comprehensively?
        - When assessing research gaps: What specific information am I still missing?
        - Before concluding research: Can I provide a complete answer now?
        
        Reflection should address:
        1. Analysis of current findings - What concrete information have I gathered?
        2. Gap assessment - What crucial information is still missing?
        3. Quality evaluation - Do I have sufficient evidence/examples for a good answer?
        4. Strategic decision - Should I continue searching or provide my answer?
    """.trimIndent()

    override suspend fun doExecute(args: Args): String {
        println("\nAgent Reflection:")
        println("-".repeat(80))
        println(args.reflection)
        println("-".repeat(80))

        return "Reflection recorded: ${args.reflection}"
    }
}
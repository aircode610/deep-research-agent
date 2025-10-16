package com.research.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.structure.StructureFixingParser
import ai.koog.prompt.structure.StructuredResponse
import com.research.models.ResearchBriefSchema
import com.research.prompts.ResearchPrompts  // ✅ Updated import

/**
 * Agent that generates research briefs
 */
class BriefGenerationAgent(apiKey: String) {

    private val promptExecutor = simpleOpenAIExecutor(apiKey)

    /**
     * Generate research brief using structured output
     */
    suspend fun generateBrief(conversationHistory: List<String>): String {
        val strategy = strategy<String, String>("brief-generation") {
            val setup by node<String, String> { input -> input }

            val briefNode by nodeLLMRequestStructured<ResearchBriefSchema>(
                name = "generate-brief",
                fixingParser = StructureFixingParser(
                    fixingModel = OpenAIModels.Chat.GPT4o,
                    retries = 2
                )
            )

            val processResult by node<Result<StructuredResponse<ResearchBriefSchema>>, String> { result ->
                when {
                    result.isSuccess -> {
                        result.getOrNull()?.structure?.researchBrief
                            ?: throw IllegalStateException("No structure in successful result")
                    }
                    result.isFailure -> {
                        throw result.exceptionOrNull()
                            ?: IllegalStateException("Failed to get structured response")
                    }
                    else -> {
                        throw IllegalStateException("Unknown result state")
                    }
                }
            }

            edge(nodeStart forwardTo setup)
            edge(setup forwardTo briefNode)
            edge(briefNode forwardTo processResult)
            edge(processResult forwardTo nodeFinish)
        }

        val agentConfig = AIAgentConfig(
            prompt = prompt("brief-generation") {
                system(ResearchPrompts.createBriefPrompt(conversationHistory))  // ✅ Updated
            },
            model = OpenAIModels.CostOptimized.GPT4oMini,
            maxAgentIterations = 5
        )

        val agent = AIAgent(
            promptExecutor = promptExecutor,
            toolRegistry = ToolRegistry.EMPTY,
            strategy = strategy,
            agentConfig = agentConfig
        )

        return agent.run("Generate research brief")
    }
}
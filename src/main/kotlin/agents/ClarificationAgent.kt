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
import com.research.models.ClarificationResponse
import com.research.prompts.ResearchPrompts

/**
 * Agent that handles clarification questions
 */
class ClarificationAgent(apiKey: String) {

    private val promptExecutor = simpleOpenAIExecutor(apiKey)

    /**
     * Check if clarification is needed using structured output
     */
    suspend fun checkClarification(conversationHistory: List<String>): ClarificationResponse {
        val strategy = strategy<String, ClarificationResponse>("clarification-check") {
            val setup by node<String, String> { input -> input }

            val clarifyNode by nodeLLMRequestStructured<ClarificationResponse>(
                name = "check-clarification",
                fixingParser = StructureFixingParser(
                    fixingModel = OpenAIModels.Chat.GPT4o,
                    retries = 2
                )
            )

            val processResult by node<Result<StructuredResponse<ClarificationResponse>>, ClarificationResponse> { result ->
                when {
                    result.isSuccess -> {
                        result.getOrNull()?.structure
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
            edge(setup forwardTo clarifyNode)
            edge(clarifyNode forwardTo processResult)
            edge(processResult forwardTo nodeFinish)
        }

        val agentConfig = AIAgentConfig(
            prompt = prompt("clarification") {
                system(ResearchPrompts.createClarificationPrompt(conversationHistory))
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

        return agent.run("Check if clarification is needed")
    }
}
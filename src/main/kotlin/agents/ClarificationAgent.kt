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
import com.research.models.ResearchBriefSchema
import com.research.prompts.ClarificationPrompt

/**
 * Simple clarification agent that asks questions until ready to proceed
 */
class ClarificationAgent(private val apiKey: String) {

    private val promptExecutor = simpleOpenAIExecutor(apiKey)

    /**
     * Conversation message holder
     */
    data class ConversationTurn(
        val role: String, // "user" or "assistant"
        val content: String
    )

    /**
     * Run clarification loop
     * Returns the final research brief when ready
     */
    suspend fun clarify(initialQuery: String): String {
        val conversation = mutableListOf<ConversationTurn>()
        conversation.add(ConversationTurn("user", initialQuery))

        // Clarification loop
        while (true) {
            println("\n--- Checking if clarification needed ---")

            val clarificationResponse = checkClarification(conversation)

            if (!clarificationResponse.needClarification) {
                println("\n✓ Ready to proceed!")
                println("Verification: ${clarificationResponse.verification}")
                break
            }

            // Need clarification
            println("\nAssistant: ${clarificationResponse.question}")
            conversation.add(ConversationTurn("assistant", clarificationResponse.question))

            // Get user response
            print("\nYou: ")
            val userResponse = readLine() ?: ""
            conversation.add(ConversationTurn("user", userResponse))
        }

        // Generate research brief
        println("\n--- Generating research brief ---")
        val brief = generateBrief(conversation)
        println("\n✓ Research brief generated:")
        println(brief)

        return brief
    }

    /**
     * Check if clarification is needed using structured output
     */
    private suspend fun checkClarification(conversation: List<ConversationTurn>): ClarificationResponse {
        val strategy = strategy<String, ClarificationResponse>("clarification-check") {
            // Setup node that provides the trigger message
            val setup by node<String, String> { input -> input }

            // Node that requests structured output
            val clarifyNode by nodeLLMRequestStructured<ClarificationResponse>(
                name = "check-clarification",
                fixingParser = StructureFixingParser(
                    fixingModel = OpenAIModels.Chat.GPT4o,
                    retries = 2
                )
            )

            // Node that processes the result and extracts the structure
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

        // Build prompt with conversation history
        val agentConfig = AIAgentConfig(
            prompt = prompt("clarification") {
                system(ClarificationPrompt.createClarificationPrompt(conversation))
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

        // Trigger with a simple message
        return agent.run("Check if clarification is needed")
    }

    /**
     * Generate research brief using structured output
     */
    private suspend fun generateBrief(conversation: List<ConversationTurn>): String {
        val strategy = strategy<String, String>("brief-generation") {
            // Setup node
            val setup by node<String, String> { input -> input }

            // Node that requests structured output
            val briefNode by nodeLLMRequestStructured<ResearchBriefSchema>(
                name = "generate-brief",
                fixingParser = StructureFixingParser(
                    fixingModel = OpenAIModels.Chat.GPT4o,
                    retries = 2
                )
            )

            // Node that processes the result and extracts the research brief
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
                system(ClarificationPrompt.createBriefPrompt(conversation))
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

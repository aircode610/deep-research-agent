package com.research

import com.research.agents.ClarificationAgent
import com.research.agents.BriefGenerationAgent
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("=== Deep Research Agent - Clarification System ===\n")

    val apiKey = System.getenv("OPENAI_API_KEY")
        ?: error("Please set OPENAI_API_KEY environment variable")

    print("Enter your research topic: ")
    val initialQuery = readlnOrNull() ?: error("No input provided")

    // Initialize agents
    val clarificationAgent = ClarificationAgent(apiKey)
    val briefGenerationAgent = BriefGenerationAgent(apiKey)

    // Track conversation
    val conversation = mutableListOf<ClarificationAgent.ConversationTurn>()
    conversation.add(ClarificationAgent.ConversationTurn("user", initialQuery))

    // Clarification loop
    while (true) {
        println("\n--- Checking if clarification needed ---")

        val clarificationResponse = clarificationAgent.checkClarification(conversation)

        if (!clarificationResponse.needClarification) {
            println("\n✓ Ready to proceed!")
            println("Verification: ${clarificationResponse.verification}")
            break
        }

        // Need clarification
        println("\nAssistant: ${clarificationResponse.question}")
        conversation.add(ClarificationAgent.ConversationTurn("assistant", clarificationResponse.question))

        // Get user response
        print("\nYou: ")
        val userResponse = readlnOrNull() ?: ""
        conversation.add(ClarificationAgent.ConversationTurn("user", userResponse))
    }

    // Generate research brief
    println("\n--- Generating research brief ---")
    val briefConversation = conversation.map {
        BriefGenerationAgent.ConversationTurn(it.role, it.content)
    }
    val researchBrief = briefGenerationAgent.generateBrief(briefConversation)

    println("\n✓ Research brief generated:")
    println(researchBrief)

    println("\n" + "=".repeat(50))
    println("FINAL RESEARCH BRIEF:")
    println("=".repeat(50))
    println(researchBrief)
}

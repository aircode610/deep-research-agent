package com.research

import com.research.agents.ClarificationAgent
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("=== Deep Research Agent - Clarification System ===\n")

    val apiKey = System.getenv("OPENAI_API_KEY")
        ?: error("Please set OPENAI_API_KEY environment variable")

    print("Enter your research topic: ")
    val initialQuery = readlnOrNull() ?: error("No input provided")

    val agent = ClarificationAgent(apiKey)
    val researchBrief = agent.clarify(initialQuery)

    println("\n" + "=".repeat(50))
    println("FINAL RESEARCH BRIEF:")
    println("=".repeat(50))
    println(researchBrief)
}
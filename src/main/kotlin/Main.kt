package com.research

import com.research.workflows.ScopeWorkflow
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("=== Deep Research Agent - Scoping System ===\n")

    val apiKey = System.getenv("OPENAI_API_KEY")
        ?: error("Please set OPENAI_API_KEY environment variable")

    print("Enter your research topic: ")
    val initialQuery = readlnOrNull() ?: error("No input provided")

    // Execute complete scoping workflow
    val scopeWorkflow = ScopeWorkflow(apiKey)
    val scopeResult = scopeWorkflow.executeScopeWorkflow(initialQuery)

    // Display final result
    println("\n" + "=".repeat(50))
    println("SCOPING COMPLETE")
    println("=".repeat(50))
    println("\nVerification: ${scopeResult.verification}")
    println("\nFinal Research Brief:")
    println(scopeResult.researchBrief)
    println("\n" + "=".repeat(50))
}
package com.research
//
//import com.research.evaluation.scope.ScopeEvaluationRunner
//import com.research.workflows.ScopeWorkflow
//import kotlinx.coroutines.runBlocking
//
//fun main() = runBlocking {
//    println("=== Deep Research Agent - Scoping System ===\n")
//
//    val apiKey = System.getenv("OPENAI_API_KEY")
//        ?: error("Please set OPENAI_API_KEY environment variable")
//
//    print("Run scope evaluation (y/n): ")
//    val evaluationRunner = ScopeEvaluationRunner(apiKey)
//    if (readlnOrNull()?.lowercase() == "y") {
//        val passed = evaluationRunner.run()
//        println("Evaluation ${if (passed) "succeeded ✅\n" else "failed ❌\n"}")
//    }
//
//    print("Enter your research topic: ")
//    val initialQuery = readlnOrNull() ?: error("No input provided")
//
//    // Scope workflow
//    val scopeWorkflow = ScopeWorkflow(apiKey)
//    val scopeResult = scopeWorkflow.executeScopeWorkflow(initialQuery)
//
//    println("\n" + "=".repeat(50))
//    println("SCOPING COMPLETE")
//    println("=".repeat(50))
//    println("\nVerification: ${scopeResult.verification}")
//    println("\nFinal Research Brief:")
//    println(scopeResult.researchBrief)
//    println("\n" + "=".repeat(50))
//}

import com.research.agents.SupervisorAgent
import com.research.agents.ResearcherAgent

suspend fun main() {
    val openaiKey = System.getenv("OPENAI_API_KEY") ?: error("Set OPENAI_API_KEY")
    val tavilyKey = System.getenv("TAVILY_API_KEY") ?: error("Set TAVILY_API_KEY")

    // Create researcher agent
    val researcherAgent = ResearcherAgent(
        apiKey = openaiKey,
        tavilyApiKey = tavilyKey
    )

    // Create supervisor
    val supervisor = SupervisorAgent(
        apiKey = openaiKey,
        researcherAgent = researcherAgent,
        maxConcurrentResearchers = 3,
        maxIterations = 12
    )

    // Run supervised research
    val result = supervisor.research(
        researchBrief = "Compare the AI safety approaches of OpenAI and Anthropic"
    )

    println("\n" + "=".repeat(80))
    println("SUPERVISOR RESULTS")
    println("=".repeat(80))
    println("Total research notes: ${result.notes.size}")
    result.notes.forEachIndexed { index : Int, note : String ->
        println("\n--- Note ${index + 1} ---")
        println(note.take(200))
        println("...")
    }
}
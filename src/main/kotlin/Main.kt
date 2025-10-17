//package com.research
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

package com.research

import com.research.agents.FinalReportAgent
import com.research.agents.ResearcherAgent
import com.research.agents.SupervisorAgent
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val openaiKey = System.getenv("OPENAI_API_KEY") ?: error("Set OPENAI_API_KEY")
    val tavilyKey = System.getenv("TAVILY_API_KEY") ?: error("Set TAVILY_API_KEY")

    println("=== Deep Research Agent - Full Pipeline ===\n")

    // Test research brief
    val researchBrief = "Compare the AI safety approaches of OpenAI and Anthropic"

    // Step 1: Create agents
    val researcherAgent = ResearcherAgent(
        apiKey = openaiKey,
        tavilyApiKey = tavilyKey
    )

    val supervisorAgent = SupervisorAgent(
        apiKey = openaiKey,
        researcherAgent = researcherAgent,
        maxConcurrentResearchers = 2, // Reduced for rate limits
        maxIterations = 15
    )

    val reportAgent = FinalReportAgent(apiKey = openaiKey)

    // Step 2: Execute supervised research
    println("PHASE 1: RESEARCH")
    println("=".repeat(80))
    val supervisorResult = supervisorAgent.research(researchBrief)

    println("\n" + "=".repeat(80))
    println("RESEARCH PHASE COMPLETE")
    println("Total findings: ${supervisorResult.notes.size}")
    println("=".repeat(80) + "\n")

    // Step 3: Generate final report
    println("PHASE 2: REPORT GENERATION")
    println("=".repeat(80))
    val finalReport = reportAgent.generateReport(
        researchBrief = researchBrief,
        findings = supervisorResult.notes
    )

    // Step 4: Display final report
    println("\n" + "=".repeat(80))
    println("FINAL RESEARCH REPORT")
    println("=".repeat(80) + "\n")
    println(finalReport)
    println("\n" + "=".repeat(80))
}
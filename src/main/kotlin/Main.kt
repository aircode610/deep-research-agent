package com.research

import com.research.workflows.ReportWorkflow
import com.research.workflows.ResearchWorkflow
import com.research.workflows.ScopeWorkflow
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("=".repeat(80))
    println("=== DEEP RESEARCH AGENT - FULL PIPELINE ===")
    println("=".repeat(80) + "\n")

    // API Keys
    val openaiKey = System.getenv("OPENAI_API_KEY")
        ?: error("Please set OPENAI_API_KEY environment variable")
    val tavilyKey = System.getenv("TAVILY_API_KEY")
        ?: error("Please set TAVILY_API_KEY environment variable")

    // Research topic
    print("Enter your research topic: ")
    val initialQuery = readlnOrNull() ?: error("No input provided")

    try {
        // Scoping
        println("\n" + "=".repeat(80))
        println("PHASE 1: SCOPING")
        println("=".repeat(80))

        val scopeWorkflow = ScopeWorkflow(openaiKey)
        val scopeResult = scopeWorkflow.executeScopeWorkflow(initialQuery)

        println("\nScoping Complete")
        println("Research Brief: ${scopeResult.researchBrief.take(100)}...")

        // Research
        println("\n" + "=".repeat(80))
        println("PHASE 2: RESEARCH")
        println("=".repeat(80))

        val researchWorkflow = ResearchWorkflow(
            apiKey = openaiKey,
            tavilyApiKey = tavilyKey,
            maxConcurrentResearchers = 2,
            maxIterations = 20
        )
        val researchResult = researchWorkflow.executeResearch(scopeResult.researchBrief)

        println("Research Complete")
        println("Findings collected: ${researchResult.findings.size}")

        // Report Generation
        println("\n" + "=".repeat(80))
        println("PHASE 3: REPORT GENERATION")
        println("=".repeat(80))

        val reportWorkflow = ReportWorkflow(openaiKey)
        val finalReport = reportWorkflow.generateReport(
            researchBrief = scopeResult.researchBrief,
            findings = researchResult.findings
        )

        // Report
        println("\n" + "=".repeat(80))
        println("=== FINAL RESEARCH REPORT ===")
        println("=".repeat(80) + "\n")
        println(finalReport)
        println("\n" + "=".repeat(80))
        println("ALL PHASES COMPLETE")
        println("=".repeat(80))

    } catch (e: Exception) {
        println("\n" + "=".repeat(80))
        println("ERROR: ${e.message}")
        println("=".repeat(80))
        e.printStackTrace()
    }
}
package com.research

import com.research.evaluation.scope.ScopeEvaluationDataset
import com.research.evaluation.scope.ScopeEvaluator
import com.research.workflows.ScopeWorkflow
import kotlinx.coroutines.runBlocking

//fun main() = runBlocking {
//    println("=== Deep Research Agent - Scoping System ===\n")
//
//    val apiKey = System.getenv("OPENAI_API_KEY")
//        ?: error("Please set OPENAI_API_KEY environment variable")
//
//    print("Enter your research topic: ")
//    val initialQuery = readlnOrNull() ?: error("No input provided")
//
//    // Execute complete scoping workflow
//    val scopeWorkflow = ScopeWorkflow(apiKey)
//    val scopeResult = scopeWorkflow.executeScopeWorkflow(initialQuery)
//
//    // Display final result
//    println("\n" + "=".repeat(50))
//    println("SCOPING COMPLETE")
//    println("=".repeat(50))
//    println("\nVerification: ${scopeResult.verification}")
//    println("\nFinal Research Brief:")
//    println(scopeResult.researchBrief)
//    println("\n" + "=".repeat(50))
//}

fun main() = runBlocking {
    val apiKey = System.getenv("OPENAI_API_KEY")
        ?: error("Please set OPENAI_API_KEY environment variable")

    val evaluator = ScopeEvaluator(apiKey)
    val scopeWorkflow = ScopeWorkflow(apiKey)

    // Run evaluation
    val results = evaluator.runEvaluation(
        testCases = ScopeEvaluationDataset.testCases,
        generateBrief = { input ->
            // Generate brief for each test case
            val result = scopeWorkflow.executeScopeWorkflow(input)
            result.researchBrief
        }
    )

    // Exit code based on results
    val avgScore = results.map { it.score }.average()
    val exitCode = if (avgScore >= 0.8) 0 else 1

    println("\n${if (exitCode == 0) "✓ EVALUATION PASSED" else "✗ EVALUATION FAILED"}")
//    kotlin.system.exitProcess(exitCode)
}
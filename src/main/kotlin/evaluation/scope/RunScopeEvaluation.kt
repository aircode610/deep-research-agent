package com.research.evaluation.scope

import com.research.workflows.ScopeWorkflow
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
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
    kotlin.system.exitProcess(exitCode)
}
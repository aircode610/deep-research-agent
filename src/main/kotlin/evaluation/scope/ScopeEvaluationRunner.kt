package com.research.evaluation.scope

import com.research.workflows.ScopeWorkflow
import kotlinx.coroutines.runBlocking

class ScopeEvaluationRunner(private val apiKey: String) {

    fun run(): Boolean = runBlocking {
        val evaluator = ScopeEvaluator(apiKey)
        val scopeWorkflow = ScopeWorkflow(apiKey)

        val results = evaluator.runEvaluation(
            testCases = ScopeEvaluationDataset.testCases,
            generateBrief = { input ->
                val result = scopeWorkflow.executeScopeWorkflow(input)
                result.researchBrief
            }
        )

        val avgScore = results.map { it.score }.average()
        val passed = avgScore >= 0.8

        println("\n${if (passed) "✓ EVALUATION PASSED" else "✗ EVALUATION FAILED"}")
        return@runBlocking passed
    }
}

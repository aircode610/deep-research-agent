package com.research.evaluation.scope

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import kotlinx.coroutines.delay

/**
 * Simple evaluator for scope workflow using direct LLM calls
 */
class ScopeEvaluator(private val apiKey: String) {

    private val executor = simpleOpenAIExecutor(apiKey)

    data class PointResult(
        val point: String,
        val included: Boolean,
        val reasoning: String
    )

    data class TestResult(
        val testCase: ScopeTestCase,
        val generatedBrief: String,
        val pointResults: List<PointResult>,
        val score: Double
    )

    /**
     * Evaluate if a point is included in the brief
     */
    suspend fun evaluatePoint(brief: String, expectedPoint: String): PointResult {
        val evaluationPrompt = prompt("evaluate-point") {
            system("""
                You are evaluating if a research brief includes a specific point.
                
                RESEARCH BRIEF:
                $brief
                
                EXPECTED POINT:
                $expectedPoint
                
                Does the brief include this point (explicitly or implicitly)?
                
                Respond in this exact format:
                INCLUDED: yes/no
                REASONING: <your reasoning>
            """.trimIndent())
            user("Evaluate the point")
        }

        val response = executor.execute(
            evaluationPrompt,
            OpenAIModels.Chat.GPT4o
        ).first()  // ✅ Get first response

        val content = response.content  // ✅ Get content from response
        val included = content.contains("INCLUDED: yes", ignoreCase = true)
        val reasoning = content.substringAfter("REASONING:", "").trim()

        return PointResult(
            point = expectedPoint,
            included = included,
            reasoning = reasoning
        )
    }

    /**
     * Evaluate a complete test case
     */
    suspend fun evaluateTestCase(testCase: ScopeTestCase, generatedBrief: String): TestResult {
        println("\nEvaluating ${testCase.id}...")
        println("Input: ${testCase.input}")

        val pointResults = mutableListOf<PointResult>()

        for (point in testCase.expectedPoints) {
            val result = evaluatePoint(generatedBrief, point)
            pointResults.add(result)

            val status = if (result.included) "✓" else "✗"
            println("  $status ${point}")

            delay(1000) // Rate limiting
        }

        val score = pointResults.count { it.included }.toDouble() / pointResults.size
        println("Score: ${String.format("%.1f", score * 100)}%")

        return TestResult(
            testCase = testCase,
            generatedBrief = generatedBrief,
            pointResults = pointResults,
            score = score
        )
    }

    /**
     * Run evaluation on all test cases
     */
    suspend fun runEvaluation(
        testCases: List<ScopeTestCase>,
        generateBrief: suspend (String) -> String
    ): List<TestResult> {
        println("=== Scope Workflow Evaluation ===")
        println("Total test cases: ${testCases.size}\n")

        val results = mutableListOf<TestResult>()

        for (testCase in testCases) {
            val brief = generateBrief(testCase.input)
            val result = evaluateTestCase(testCase, brief)
            results.add(result)

            delay(2000) // Rate limiting between tests
        }

        printSummary(results)
        return results
    }

    private fun printSummary(results: List<TestResult>) {
        println("\n" + "=".repeat(60))
        println("EVALUATION SUMMARY")
        println("=".repeat(60))

        val avgScore = results.map { it.score }.average()
        val passed = results.count { it.score >= 0.8 }

        println("Total Tests: ${results.size}")
        println("Passed (≥80%): $passed")
        println("Failed (<80%): ${results.size - passed}")
        println("Average Score: ${String.format("%.1f%%", avgScore * 100)}")
        println("=".repeat(60))

        results.forEach { result ->
            val status = if (result.score >= 0.8) "✓ PASS" else "✗ FAIL"
            println("${result.testCase.id}: ${String.format("%.1f%%", result.score * 100)} $status")
        }
    }
}
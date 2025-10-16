package com.research.evaluation.scope

/**
 * Test case for scope evaluation
 */
data class ScopeTestCase(
    val id: String,
    val input: String,
    val expectedPoints: List<String>
)

/**
 * Dataset for evaluating the scoping workflow
 */
object ScopeEvaluationDataset {

    val testCases = listOf(
        ScopeTestCase(
            id = "SCOPE-001",
            input = "I want to research coffee shops in San Francisco to see which one is the best to have a good time with good vibe, prices, and other qualities.",
            expectedPoints = listOf(
                "Focus on coffee shops located in San Francisco",
                "Consider factors like quality, ambiance, and service",
                "No specific budget constraints mentioned"
            )
        ),

        ScopeTestCase(
            id = "SCOPE-002",
            input = "Compare OpenAI vs Anthropic approaches to AI safety",
            expectedPoints = listOf(
                "Research OpenAI's AI safety methodologies",
                "Research Anthropic's AI safety methodologies",
                "Compare and contrast the two approaches"
            )
        ),

        ScopeTestCase(
            id = "SCOPE-003",
            input = "Find 2-bedroom apartments in Manhattan under $3000 per month",
            expectedPoints = listOf(
                "Geographic constraint: Manhattan",
                "Budget constraint: under $3000 per month",
                "Size requirement: 2-bedroom apartments"
            )
        ),

        ScopeTestCase(
            id = "SCOPE-004",
            input = "I have a budget of $400 and I want to travel. What are the best noise-cancelling headphones.",
            expectedPoints = listOf(
                "Product category: noise-cancelling headphones",
                "Use case: travel",
                "Budget constraint: under $400"
            )
        ),

        ScopeTestCase(
            id = "SCOPE-005",
            input = "Market entry strategy for a SaaS startup in Germany considering competitors and market analysis",
            expectedPoints = listOf(
                "Focus on market entry strategies",
                "Business type: SaaS startup",
                "Geographic target: Germany market"
            )
        )
    )
}
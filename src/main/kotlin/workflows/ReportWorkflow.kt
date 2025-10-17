package com.research.workflows

import com.research.agents.FinalReportAgent

/**
 * Report generation workflow that synthesizes research findings into a final report.
 */
class ReportWorkflow(apiKey: String) {

    private val reportAgent = FinalReportAgent(apiKey)

    /**
     * Execute the complete report generation workflow
     */
    suspend fun generateReport(
        researchBrief: String,
        findings: List<String>
    ): String {
        println("\n" + "=".repeat(80))
        println("=== REPORT GENERATION PHASE ===")
        println("=".repeat(80))
        println("Synthesizing ${findings.size} research findings...")
        println("=".repeat(80) + "\n")

        val report = reportAgent.generateReport(
            researchBrief = researchBrief,
            findings = findings
        )

        println("\n" + "=".repeat(80))
        println("REPORT GENERATION COMPLETE")
        println("=".repeat(80) + "\n")

        return report
    }
}
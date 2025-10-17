package com.research.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

/**
 * Tavily Search Tool API
 */
class TavilySearchTool(
    private val apiKey: String,
    private val onSearchExecuted: ((query: String, result: String) -> Unit)? = null
) : SimpleTool<TavilySearchTool.Args>() {

    @Serializable
    data class Args(
        @property:LLMDescription("The search query to execute")
        val query: String,

        @property:LLMDescription("Search depth: 'basic' or 'advanced' (default: basic)")
        val searchDepth: String = "basic",

        @property:LLMDescription("Maximum number of results to return between 1-10 (default: 5)")
        val maxResults: Int = 5,

        @property:LLMDescription("Include raw webpage content for detailed analysis (default: true)")
        val includeRawContent: Boolean = true,

        @property:LLMDescription("Topic filter: 'general' or 'news' (default: general)")
        val topic: String = "general"
    )

    override val argsSerializer = Args.serializer()

    override val description = """
        Search the web using Tavily's powerful search API.
        Returns ranked search results with content snippets and optional raw content.
        Use 'advanced' search depth for higher quality, more relevant results.
        Use 'news' topic filter for recent news articles.
    """.trimIndent()

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    override suspend fun doExecute(args: Args): String {
        return try {
            val response = httpClient.post("https://api.tavily.com/search") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("api_key", apiKey)
                    put("query", args.query)
                    put("search_depth", args.searchDepth)
                    put("max_results", args.maxResults)
                    put("include_raw_content", args.includeRawContent)
                    put("topic", args.topic)
                })
            }

            val jsonResponse: JsonObject = response.body()
            val formattedResult = formatSearchResults(jsonResponse)

            onSearchExecuted?.invoke(args.query, formattedResult)

            formattedResult

        } catch (e: Exception) {
            "Error performing search: ${e.message}"
        }
    }

    private fun formatSearchResults(response: JsonObject): String {
        val results = response["results"]?.jsonArray ?: return "No results found"
        val answer = response["answer"]?.jsonPrimitive?.content

        val formatted = buildString {
            if (!answer.isNullOrBlank()) {
                appendLine("AI Summary:")
                appendLine(answer)
                appendLine("\n" + "=".repeat(80) + "\n")
            }

            appendLine("Search Results:\n")

            results.forEachIndexed { index, result ->
                val resultObj = result.jsonObject
                val title = resultObj["title"]?.jsonPrimitive?.content ?: "No title"
                val url = resultObj["url"]?.jsonPrimitive?.content ?: ""
                val content = resultObj["content"]?.jsonPrimitive?.content ?: ""
                val score = resultObj["score"]?.jsonPrimitive?.doubleOrNull

                appendLine("--- SOURCE ${index + 1}: $title ---")
                appendLine("URL: $url")
                if (score != null) {
                    appendLine("Relevance Score: ${"%.2f".format(score)}")
                }
                appendLine("\nSUMMARY:")
                appendLine(content)
                appendLine("\n" + "-".repeat(80) + "\n")
            }
        }

        return formatted
    }

    fun close() {
        httpClient.close()
    }
}
package com.research.prompts

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * All prompts for the research scoping workflow
 */
object ResearchPrompts {

    private fun getTodayStr(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEE MMM d, yyyy"))
    }

    /**
     * Create prompt for clarification decision
     */
    fun createClarificationPrompt(conversationHistory: List<String>): String {
        val messagesStr = conversationHistory.joinToString("\n")

        return """
        These are the messages that have been exchanged so far from the user asking for the report:
        <Messages>
        $messagesStr
        </Messages>

        Today's date is ${getTodayStr()}.

        Assess whether you need to ask a clarifying question, or if the user has already provided enough information for you to start research.
        IMPORTANT: If you can see in the messages history that you have already asked a clarifying question, you almost always do not need to ask another one. Only ask another question if ABSOLUTELY NECESSARY.

        If there are acronyms, abbreviations, or unknown terms, ask the user to clarify.
        If you need to ask a question, follow these guidelines:
        - Be concise while gathering all necessary information
        - Make sure to gather all the information needed to carry out the research task in a concise, well-structured manner.
        - Use bullet points or numbered lists if appropriate for clarity.
        - Don't ask for unnecessary information, or information that the user has already provided.

        If you need to ask a clarifying question, return:
        needClarification: true,
        question: "<your clarifying question>",
        verification: ""

        If you do not need to ask a clarifying question, return:
        needClarification: false,
        question: "",
        verification: "<acknowledgement message that you will now start research based on the provided information>"

        For the verification message when no clarification is needed:
        - Acknowledge that you have sufficient information to proceed
        - Briefly summarize the key aspects of what you understand from their request
        - Confirm that you will now begin the research process
        - Keep the message concise and professional
        """.trimIndent()
    }

    /**
     * Create prompt for research brief generation
     */
    fun createBriefPrompt(conversationHistory: List<String>): String {
        val messagesStr = conversationHistory.joinToString("\n")

        return """
        You will be given a set of messages that have been exchanged so far between yourself and the user. 
        Your job is to translate these messages into a more detailed and concrete research question that will be used to guide the research.

        The messages that have been exchanged so far between yourself and the user are:
        <Messages>
        $messagesStr
        </Messages>

        Today's date is ${getTodayStr()}.

        You will return a single research question that will be used to guide the research.

        Guidelines:
        1. Maximize Specificity and Detail
        - Include all known user preferences and explicitly list key attributes or dimensions to consider.
        - It is important that all details from the user are included in the instructions.

        2. Handle Unstated Dimensions Carefully
        - When research quality requires considering additional dimensions that the user hasn't specified, acknowledge them as open considerations rather than assumed preferences.
        - Example: Instead of assuming "budget-friendly options," say "consider all price ranges unless cost constraints are specified."

        3. Avoid Unwarranted Assumptions
        - Never invent specific user preferences, constraints, or requirements that weren't stated.
        - If the user hasn't provided a particular detail, explicitly note this lack of specification.

        4. Distinguish Between Research Scope and User Preferences
        - Research scope: What topics/dimensions should be investigated (can be broader than user's explicit mentions)
        - User preferences: Specific constraints, requirements, or preferences (must only include what user stated)

        5. Use the First Person
        - Phrase the request from the perspective of the user.

        6. Sources
        - If specific sources should be prioritized, specify them in the research question.
        """.trimIndent()
    }

    /**
     * System prompt for individual researcher agent
     */
    fun createResearcherPrompt(): String {
        return """
        You are a research assistant conducting research on the user's input topic. For context, today's date is ${getTodayStr()}.

        <Task>
        Your job is to use tools to gather information about the user's input topic.
        You can use any of the tools provided to you to find resources that can help answer the research question. 
        You can call these tools in series or in parallel, your research is conducted in a tool-calling loop.
        </Task>

        <Available Tools>
        You have access to two main tools:
        1. **tavily_search**: For conducting web searches to gather information
        2. **think_tool**: For reflection and strategic planning during research

        **CRITICAL: Use think_tool after each search to reflect on results and plan next steps**
        </Available Tools>

        <Instructions>
        Think like a human researcher with limited time. Follow these steps:

        1. **Read the question carefully** - What specific information does the user need?
        2. **Start with broader searches** - Use broad, comprehensive queries first
        3. **After each search, pause and assess** - Do I have enough to answer? What's still missing?
        4. **Execute narrower searches as you gather information** - Fill in the gaps
        5. **Stop when you can answer confidently** - Don't keep searching for perfection
        </Instructions>

        <Hard Limits>
        **Tool Call Budgets** (Prevent excessive searching):
        - **Simple queries**: Use 2-3 search tool calls maximum
        - **Complex queries**: Use up to 5 search tool calls maximum
        - **Always stop**: After 5 search tool calls if you cannot find the right sources

        **Stop Immediately When**:
        - You can answer the user's question comprehensively
        - You have 3+ relevant examples/sources for the question
        - Your last 2 searches returned similar information
        </Hard Limits>

        <Show Your Thinking>
        After each search tool call, use think_tool to analyze the results:
        - What key information did I find?
        - What's missing?
        - Do I have enough to answer the question comprehensively?
        - Should I search more or provide my answer?
        </Show Your Thinking>
        """.trimIndent()
    }

    /**
     * Prompt for compressing research findings
     */
    fun createCompressionPrompt() : String {
        return """
    You are a research assistant that has conducted research on a topic by calling several tools and web searches. 
    Your job is now to clean up the findings, but preserve all of the relevant statements and information that the researcher has gathered. 
    For context, today's date is ${getTodayStr()}.

    <Task>
    You will receive:
    1. A list of search queries that were executed
    2. Raw search results with URLs and content from each search
    3. The researcher's final analysis
    
    Your job is to synthesize all of this into a comprehensive report that preserves all relevant information, especially the URLs and sources.
    </Task>

    <Tool Call Filtering>
    **IMPORTANT**: When processing the research:
    - **Include**: All search results, findings, and sources with their URLs
    - **Exclude**: Any "Reflection recorded" messages - these are internal reflections
    - **Focus on**: Actual information gathered from external sources with proper attribution
    </Tool Call Filtering>

    <Guidelines>
    1. Your output findings should be fully comprehensive and include ALL information and sources gathered.
    2. **CRITICAL**: Preserve all URLs exactly as they appear in the raw search results.
    3. Return inline citations for each source using the format [1], [2], etc.
    4. Include a "Sources" section at the end with all URLs.
    5. Make sure to include ALL sources and their URLs - this is critical.
    6. Structure the report to be clear and well-organized.
    </Guidelines>

    <Output Format>
    The report should be structured like this:
    
    **List of Queries and Tool Calls Made**
    [List each search query that was executed]
    
    **Fully Comprehensive Findings**
    [Organized findings with inline citations like [1], [2]]
    
    ### Sources
    [1] Source Title: [Full URL here]
    [2] Source Title: [Full URL here]
    </Output Format>

    <Citation Rules>
    - Assign each unique URL a single citation number
    - Use the format: [1] Source Title: https://full-url-here.com
    - Number sources sequentially without gaps (1,2,3,4...)
    - **NEVER** write "URL not provided" - the URLs are in the raw search results
    </Citation Rules>

    Critical Reminder: 
    - Preserve all URLs from the raw search results
    - Do NOT summarize or paraphrase - preserve information verbatim
    - Include ALL sources found during research
    """.trimIndent()
    }
}
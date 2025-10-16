package com.research.prompts

import com.research.agents.ClarificationAgent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ClarificationPrompt {

    private fun getTodayStr(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEE MMM d, yyyy"))
    }

    /**
     * Format conversation turns into a string for the prompt
     */
    private fun formatConversation(conversation: List<ClarificationAgent.ConversationTurn>): String {
        return conversation.joinToString("\n") { turn ->
            when (turn.role) {
                "user" -> "User: ${turn.content}"
                "assistant" -> "Assistant: ${turn.content}"
                else -> turn.content
            }
        }
    }

    fun createClarificationPrompt(conversation: List<ClarificationAgent.ConversationTurn>): String {
        val messagesStr = formatConversation(conversation)

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

    fun createBriefPrompt(conversation: List<ClarificationAgent.ConversationTurn>): String {
        val messagesStr = formatConversation(conversation)

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
}
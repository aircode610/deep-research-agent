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
}
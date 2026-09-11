package com.example.data.db.reasoning

import com.example.data.model.Question

/**
 * Aggregator for 400 Reasoning Objective Questions with Detailed Solutions and Explanations
 * Divided across 8 modular parts (50 questions each) to optimize compilation and prevent bytecode size issues.
 */
object ReasoningQuestionBankData {

    fun getAll400ReasoningQuestions(): List<Question> {
        val questions = ArrayList<Question>(400)
        questions.addAll(ReasoningQuestionsPart1.getQuestions())
        questions.addAll(ReasoningQuestionsPart2.getQuestions())
        questions.addAll(ReasoningQuestionsPart3.getQuestions())
        questions.addAll(ReasoningQuestionsPart4.getQuestions())
        questions.addAll(ReasoningQuestionsPart5.getQuestions())
        questions.addAll(ReasoningQuestionsPart6.getQuestions())
        questions.addAll(ReasoningQuestionsPart7.getQuestions())
        questions.addAll(ReasoningQuestionsPart8.getQuestions())
        return questions
    }
}

package com.example.data.db.hindi

import com.example.data.model.Question

/**
 * Aggregator for 400 Hindi Objective Questions with Detailed Explanations
 * Divided across 8 modular parts (50 questions each) to optimize compilation and prevent bytecode size issues.
 */
object HindiQuestionBankData {

    fun getAll400HindiQuestions(): List<Question> {
        val questions = ArrayList<Question>(400)
        questions.addAll(HindiQuestionsPart1.getQuestions())
        questions.addAll(HindiQuestionsPart2.getQuestions())
        questions.addAll(HindiQuestionsPart3.getQuestions())
        questions.addAll(HindiQuestionsPart4.getQuestions())
        questions.addAll(HindiQuestionsPart5.getQuestions())
        questions.addAll(HindiQuestionsPart6.getQuestions())
        questions.addAll(HindiQuestionsPart7.getQuestions())
        questions.addAll(HindiQuestionsPart8.getQuestions())
        return questions
    }
}

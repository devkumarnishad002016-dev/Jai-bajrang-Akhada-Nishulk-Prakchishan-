package com.example.data.db.english

import com.example.data.model.Question

/**
 * Aggregator for 400 English Objective Questions with Detailed Explanations
 * Divided across 8 modular parts (50 questions each) to optimize compilation and prevent bytecode size issues.
 */
object EnglishQuestionBankData {

    fun getAll400EnglishQuestions(): List<Question> {
        val questions = ArrayList<Question>(400)
        questions.addAll(EnglishQuestionsPart1.getQuestions())
        questions.addAll(EnglishQuestionsPart2.getQuestions())
        questions.addAll(EnglishQuestionsPart3.getQuestions())
        questions.addAll(EnglishQuestionsPart4.getQuestions())
        questions.addAll(EnglishQuestionsPart5.getQuestions())
        questions.addAll(EnglishQuestionsPart6.getQuestions())
        questions.addAll(EnglishQuestionsPart7.getQuestions())
        questions.addAll(EnglishQuestionsPart8.getQuestions())
        return questions
    }
}

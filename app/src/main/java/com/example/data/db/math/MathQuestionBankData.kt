package com.example.data.db.math

import com.example.data.model.Question

/**
 * Aggregator for 400 Mathematics Objective Questions with Detailed Explanations
 * Divided across 8 modular parts (50 questions each) to optimize compilation and prevent bytecode size issues.
 */
object MathQuestionBankData {

    fun getAll400MathQuestions(): List<Question> {
        val questions = ArrayList<Question>(400)
        questions.addAll(MathQuestionsPart1.getQuestions())
        questions.addAll(MathQuestionsPart2.getQuestions())
        questions.addAll(MathQuestionsPart3.getQuestions())
        questions.addAll(MathQuestionsPart4.getQuestions())
        questions.addAll(MathQuestionsPart5.getQuestions())
        questions.addAll(MathQuestionsPart6.getQuestions())
        questions.addAll(MathQuestionsPart7.getQuestions())
        questions.addAll(MathQuestionsPart8.getQuestions())
        return questions
    }
}

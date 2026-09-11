package com.example.data.db.computer

import com.example.data.model.Question

/**
 * Aggregator for 400 Computer Objective Questions with Detailed Explanations
 * Divided across 8 modular parts (50 questions each) to optimize compilation and prevent bytecode size issues.
 */
object ComputerQuestionBankData {

    fun getAll400ComputerQuestions(): List<Question> {
        val questions = ArrayList<Question>(400)
        questions.addAll(ComputerQuestionsPart1.getQuestions())
        questions.addAll(ComputerQuestionsPart2.getQuestions())
        questions.addAll(ComputerQuestionsPart3.getQuestions())
        questions.addAll(ComputerQuestionsPart4.getQuestions())
        questions.addAll(ComputerQuestionsPart5.getQuestions())
        questions.addAll(ComputerQuestionsPart6.getQuestions())
        questions.addAll(ComputerQuestionsPart7.getQuestions())
        questions.addAll(ComputerQuestionsPart8.getQuestions())
        return questions
    }
}

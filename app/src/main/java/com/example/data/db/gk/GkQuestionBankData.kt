package com.example.data.db.gk

import com.example.data.model.Question

/**
 * Aggregator for 2000 GK/GS Objective Questions with Detailed Solutions and Explanations.
 * Divided across 20 modular parts (100 questions each) to optimize compilation and prevent bytecode size issues.
 */
object GkQuestionBankData {
    fun getAll2000GkQuestions(): List<Question> {
        val questions = ArrayList<Question>(2000)
        questions.addAll(GkQuestionsPart1.getQuestions())
        questions.addAll(GkQuestionsPart2.getQuestions())
        questions.addAll(GkQuestionsPart3.getQuestions())
        questions.addAll(GkQuestionsPart4.getQuestions())
        questions.addAll(GkQuestionsPart5.getQuestions())
        questions.addAll(GkQuestionsPart6.getQuestions())
        questions.addAll(GkQuestionsPart7.getQuestions())
        questions.addAll(GkQuestionsPart8.getQuestions())
        questions.addAll(GkQuestionsPart9.getQuestions())
        questions.addAll(GkQuestionsPart10.getQuestions())
        questions.addAll(GkQuestionsPart11.getQuestions())
        questions.addAll(GkQuestionsPart12.getQuestions())
        questions.addAll(GkQuestionsPart13.getQuestions())
        questions.addAll(GkQuestionsPart14.getQuestions())
        questions.addAll(GkQuestionsPart15.getQuestions())
        questions.addAll(GkQuestionsPart16.getQuestions())
        questions.addAll(GkQuestionsPart17.getQuestions())
        questions.addAll(GkQuestionsPart18.getQuestions())
        questions.addAll(GkQuestionsPart19.getQuestions())
        questions.addAll(GkQuestionsPart20.getQuestions())
        return questions
    }
}

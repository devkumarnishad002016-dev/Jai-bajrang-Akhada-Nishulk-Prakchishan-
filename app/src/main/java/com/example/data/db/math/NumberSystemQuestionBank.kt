package com.example.data.db.math

import com.example.data.model.Question

/**
 * Aggregator for all subtopic-wise Number System questions.
 * Supplies comprehensive subtopic questions across all 9 subtopics.
 */
object NumberSystemQuestionBank {

    fun getAllNumberSystemQuestions(): List<Question> {
        val list = ArrayList<Question>(90)
        list.addAll(NumberSystemSubtopicQuestionsPart1.getQuestions())
        list.addAll(NumberSystemSubtopicQuestionsPart2.getQuestions())
        return list
    }
}

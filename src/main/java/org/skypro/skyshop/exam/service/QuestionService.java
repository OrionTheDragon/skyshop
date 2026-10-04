package org.skypro.skyshop.exam.service;

import java.util.Collection;
import org.skypro.skyshop.exam.model.Question;

public interface QuestionService {
    Question add(String question, String answer);

    Question add(Question question);

    Question remove(Question question);

    Collection<Question> getAll();

    Question getRandomQuestion();
}

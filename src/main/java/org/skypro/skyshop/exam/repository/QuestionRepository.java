package org.skypro.skyshop.exam.repository;

import java.util.Collection;
import org.skypro.skyshop.exam.model.Question;

public interface QuestionRepository {
    Question add(Question question);

    Question remove(Question question);

    Collection<Question> getAll();
}

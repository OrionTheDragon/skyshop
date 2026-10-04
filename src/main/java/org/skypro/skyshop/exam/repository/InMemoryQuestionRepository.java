package org.skypro.skyshop.exam.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.skypro.skyshop.exam.exception.QuestionNotFoundException;
import org.skypro.skyshop.exam.model.Question;

public abstract class InMemoryQuestionRepository implements QuestionRepository {
    private final Set<Question> questions = new LinkedHashSet<>();

    @Override
    public Question add(Question question) {
        questions.add(question);
        return question;
    }

    @Override
    public Question remove(Question question) {
        if (!questions.remove(question)) {
            throw new QuestionNotFoundException("Вопрос не найден");
        }
        return question;
    }

    @Override
    public Collection<Question> getAll() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(questions));
    }
}

package org.skypro.skyshop.exam.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.skypro.skyshop.exam.exception.QuestionNotFoundException;
import org.skypro.skyshop.exam.model.Question;
import org.skypro.skyshop.exam.repository.QuestionRepository;

public abstract class AbstractQuestionService implements QuestionService {
    private final QuestionRepository questionRepository;
    private final Random random;

    protected AbstractQuestionService(QuestionRepository questionRepository, Random random) {
        this.questionRepository = questionRepository;
        this.random = random;
    }

    @Override
    public Question add(String question, String answer) {
        return add(new Question(question, answer));
    }

    @Override
    public Question add(Question question) {
        return questionRepository.add(question);
    }

    @Override
    public Question remove(Question question) {
        return questionRepository.remove(question);
    }

    @Override
    public Collection<Question> getAll() {
        return questionRepository.getAll();
    }

    @Override
    public Question getRandomQuestion() {
        List<Question> questions = new ArrayList<>(questionRepository.getAll());
        if (questions.isEmpty()) {
            throw new QuestionNotFoundException("В хранилище нет вопросов");
        }
        return questions.get(random.nextInt(questions.size()));
    }
}

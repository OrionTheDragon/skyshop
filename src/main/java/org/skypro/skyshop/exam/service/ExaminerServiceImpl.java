package org.skypro.skyshop.exam.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.skypro.skyshop.exam.exception.InvalidQuestionAmountException;
import org.skypro.skyshop.exam.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ExaminerServiceImpl implements ExaminerService {
    private final QuestionService javaQuestionService;
    private final QuestionService mathQuestionService;
    private final Random random;

    @Autowired
    public ExaminerServiceImpl(@Qualifier("javaQuestionService") QuestionService javaQuestionService,
                               @Qualifier("mathQuestionService") QuestionService mathQuestionService) {
        this(javaQuestionService, mathQuestionService, new Random());
    }

    ExaminerServiceImpl(QuestionService javaQuestionService, QuestionService mathQuestionService, Random random) {
        this.javaQuestionService = javaQuestionService;
        this.mathQuestionService = mathQuestionService;
        this.random = random;
    }

    @Override
    public Collection<Question> getQuestions(int amount) {
        if (amount < 0) {
            throw new InvalidQuestionAmountException(amount, 0);
        }
        if (amount == 0) {
            return Collections.emptySet();
        }

        Set<Question> availableQuestions = new LinkedHashSet<>(javaQuestionService.getAll());
        availableQuestions.addAll(mathQuestionService.getAll());
        if (amount > availableQuestions.size()) {
            throw new InvalidQuestionAmountException(amount, availableQuestions.size());
        }

        List<Question> remainingQuestions = new ArrayList<>(availableQuestions);
        Set<Question> examQuestions = new LinkedHashSet<>();
        while (examQuestions.size() < amount) {
            int index = random.nextInt(remainingQuestions.size());
            examQuestions.add(remainingQuestions.remove(index));
        }
        return Collections.unmodifiableSet(examQuestions);
    }
}

package org.skypro.skyshop.exam.service;

import java.util.Random;
import org.skypro.skyshop.exam.repository.QuestionRepository;

class MathQuestionServiceTest extends QuestionServiceTestSupport {
    @Override
    protected QuestionService createService(QuestionRepository repository, Random random) {
        return new MathQuestionService(repository, random);
    }
}

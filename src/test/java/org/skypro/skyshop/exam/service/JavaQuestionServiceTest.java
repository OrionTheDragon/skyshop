package org.skypro.skyshop.exam.service;

import java.util.Random;
import org.skypro.skyshop.exam.repository.QuestionRepository;

class JavaQuestionServiceTest extends QuestionServiceTestSupport {
    @Override
    protected QuestionService createService(QuestionRepository repository, Random random) {
        return new JavaQuestionService(repository, random);
    }
}

package org.skypro.skyshop.exam.service;

import java.util.Random;
import org.skypro.skyshop.exam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("mathQuestionService")
public class MathQuestionService extends AbstractQuestionService {
    @Autowired
    public MathQuestionService(@Qualifier("mathQuestionRepository") QuestionRepository questionRepository) {
        this(questionRepository, new Random());
    }

    MathQuestionService(QuestionRepository questionRepository, Random random) {
        super(questionRepository, random);
    }
}

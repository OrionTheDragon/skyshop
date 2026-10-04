package org.skypro.skyshop.exam.service;

import java.util.Random;
import org.skypro.skyshop.exam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("javaQuestionService")
public class JavaQuestionService extends AbstractQuestionService {
    @Autowired
    public JavaQuestionService(@Qualifier("javaQuestionRepository") QuestionRepository questionRepository) {
        this(questionRepository, new Random());
    }

    JavaQuestionService(QuestionRepository questionRepository, Random random) {
        super(questionRepository, random);
    }
}

package org.skypro.skyshop.exam.service;

import java.util.Collection;
import org.skypro.skyshop.exam.model.Question;

public interface ExaminerService {
    Collection<Question> getQuestions(int amount);
}

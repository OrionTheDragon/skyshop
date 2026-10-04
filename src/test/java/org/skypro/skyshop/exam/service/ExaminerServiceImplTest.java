package org.skypro.skyshop.exam.service;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skypro.skyshop.exam.exception.InvalidQuestionAmountException;
import org.skypro.skyshop.exam.model.Question;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExaminerServiceImplTest {
    @Mock
    private QuestionService javaQuestionService;

    @Mock
    private QuestionService mathQuestionService;

    @Mock
    private Random random;

    private ExaminerServiceImpl examinerService;

    @BeforeEach
    void setUp() {
        examinerService = new ExaminerServiceImpl(javaQuestionService, mathQuestionService, random);
    }

    @Test
    void zeroAmountReturnsEmptyExam() {
        assertThat(examinerService.getQuestions(0)).isEmpty();
        verifyNoInteractions(javaQuestionService, mathQuestionService, random);
    }

    @Test
    void negativeAmountThrowsBadRequestException() {
        assertThrows(InvalidQuestionAmountException.class, () -> examinerService.getQuestions(-1));
        verifyNoInteractions(javaQuestionService, mathQuestionService, random);
    }

    @Test
    void positiveAmountThrowsWhenBothSubjectsAreEmpty() {
        when(javaQuestionService.getAll()).thenReturn(List.of());
        when(mathQuestionService.getAll()).thenReturn(List.of());

        assertThrows(InvalidQuestionAmountException.class, () -> examinerService.getQuestions(1));
        verifyNoInteractions(random);
    }

    @Test
    void amountAboveAvailableQuestionsThrowsBeforeRandomSelection() {
        when(javaQuestionService.getAll()).thenReturn(List.of(new Question("Java", "Answer")));
        when(mathQuestionService.getAll()).thenReturn(List.of());

        assertThrows(InvalidQuestionAmountException.class, () -> examinerService.getQuestions(2));
        verifyNoInteractions(random);
    }

    @Test
    void fullExamContainsUniqueQuestionsFromBothSubjects() {
        Question firstJava = new Question("First Java", "Answer 1");
        Question secondJava = new Question("Second Java", "Answer 2");
        Question math = new Question("2 + 2", "4");
        when(javaQuestionService.getAll()).thenReturn(List.of(firstJava, secondJava));
        when(mathQuestionService.getAll()).thenReturn(List.of(math));
        when(random.nextInt(anyInt())).thenReturn(0);

        Collection<Question> result = examinerService.getQuestions(3);

        assertThat(result).hasSize(3).containsExactlyInAnyOrder(firstJava, secondJava, math);
    }

    @Test
    void partialExamSelectsRequestedNumberFromCombinedPool() {
        Question firstJava = new Question("First Java", "Answer 1");
        Question secondJava = new Question("Second Java", "Answer 2");
        Question math = new Question("2 + 2", "4");
        when(javaQuestionService.getAll()).thenReturn(List.of(firstJava, secondJava));
        when(mathQuestionService.getAll()).thenReturn(List.of(math));
        when(random.nextInt(3)).thenReturn(2);
        when(random.nextInt(2)).thenReturn(0);

        assertThat(examinerService.getQuestions(2)).containsExactly(math, firstJava);
    }

    @Test
    void questionsRepeatedAcrossSubjectsCountAsOneAvailableQuestion() {
        when(javaQuestionService.getAll()).thenReturn(List.of(new Question("Shared", "Answer")));
        when(mathQuestionService.getAll()).thenReturn(List.of(new Question("Shared", "Answer")));

        assertThrows(InvalidQuestionAmountException.class, () -> examinerService.getQuestions(2));
        verifyNoInteractions(random);
    }

    @Test
    void sameTextWithDifferentAnswersCountsAsDifferentQuestions() {
        Question javaQuestion = new Question("Shared", "Java answer");
        Question mathQuestion = new Question("Shared", "Math answer");
        when(javaQuestionService.getAll()).thenReturn(List.of(javaQuestion));
        when(mathQuestionService.getAll()).thenReturn(List.of(mathQuestion));
        when(random.nextInt(anyInt())).thenReturn(0);

        assertThat(examinerService.getQuestions(2)).containsExactlyInAnyOrder(javaQuestion, mathQuestion);
    }
}

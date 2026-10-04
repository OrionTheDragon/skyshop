package org.skypro.skyshop.exam.service;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skypro.skyshop.exam.exception.QuestionNotFoundException;
import org.skypro.skyshop.exam.model.Question;
import org.skypro.skyshop.exam.repository.JavaQuestionRepository;
import org.skypro.skyshop.exam.repository.QuestionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
abstract class QuestionServiceTestSupport {
    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private Random random;

    private QuestionService questionService;

    protected abstract QuestionService createService(QuestionRepository repository, Random random);

    @BeforeEach
    void setUp() {
        questionService = createService(questionRepository, random);
    }

    @Test
    void addTextCreatesQuestionAndSavesItInRepository() {
        Question expected = new Question("Question", "Answer");
        when(questionRepository.add(expected)).thenReturn(expected);

        Question added = questionService.add("Question", "Answer");

        assertThat(added).isEqualTo(expected);
        verify(questionRepository).add(expected);
    }

    @Test
    void addQuestionSavesExistingObjectInRepository() {
        Question question = new Question("Question", "Answer");
        when(questionRepository.add(question)).thenReturn(question);

        assertThat(questionService.add(question)).isSameAs(question);
        verify(questionRepository).add(question);
    }

    @Test
    void addingDuplicateQuestionsKeepsOnlyOneEntry() {
        QuestionService service = createService(new JavaQuestionRepository(), new Random(0));

        service.add("Question", "Answer");
        service.add(new Question("Question", "Answer"));

        assertThat(service.getAll()).containsExactly(new Question("Question", "Answer"));
    }

    @Test
    void removeQuestionDelegatesToRepository() {
        Question question = new Question("Question", "Answer");
        when(questionRepository.remove(question)).thenReturn(question);

        assertThat(questionService.remove(question)).isSameAs(question);
        verify(questionRepository).remove(question);
    }

    @Test
    void removingQuestionFromEmptyRepositoryThrowsNotFoundException() {
        Question question = new Question("Question", "Answer");
        when(questionRepository.remove(question)).thenThrow(new QuestionNotFoundException("Вопрос не найден"));

        assertThrows(QuestionNotFoundException.class, () -> questionService.remove(question));
    }

    @Test
    void getAllReturnsEmptyCollectionForEmptyRepository() {
        when(questionRepository.getAll()).thenReturn(List.of());

        assertThat(questionService.getAll()).isEmpty();
    }

    @Test
    void getAllReturnsQuestionsFromRepository() {
        Question first = new Question("First", "Answer 1");
        Question second = new Question("Second", "Answer 2");
        when(questionRepository.getAll()).thenReturn(List.of(first, second));

        assertThat(questionService.getAll()).containsExactly(first, second);
        verify(questionRepository).getAll();
    }

    @Test
    void getRandomQuestionUsesIndexWithinCurrentCollectionSize() {
        Question first = new Question("First", "Answer 1");
        Question second = new Question("Second", "Answer 2");
        when(questionRepository.getAll()).thenReturn(List.of(first, second));
        when(random.nextInt(2)).thenReturn(1);

        assertThat(questionService.getRandomQuestion()).isSameAs(second);
        verify(random).nextInt(2);
    }

    @Test
    void getRandomQuestionReturnsOnlyQuestionWhenSizeIsOne() {
        Question question = new Question("Question", "Answer");
        when(questionRepository.getAll()).thenReturn(List.of(question));
        when(random.nextInt(1)).thenReturn(0);

        assertThat(questionService.getRandomQuestion()).isSameAs(question);
    }

    @Test
    void getRandomQuestionThrowsForEmptyRepository() {
        when(questionRepository.getAll()).thenReturn(List.of());

        assertThrows(QuestionNotFoundException.class, questionService::getRandomQuestion);
        verifyNoInteractions(random);
    }
}

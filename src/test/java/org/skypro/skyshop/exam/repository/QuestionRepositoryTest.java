package org.skypro.skyshop.exam.repository;

import java.util.Collection;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.skypro.skyshop.exam.exception.QuestionNotFoundException;
import org.skypro.skyshop.exam.model.Question;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuestionRepositoryTest {
    static Stream<QuestionRepository> repositories() {
        return Stream.of(new JavaQuestionRepository(), new MathQuestionRepository());
    }

    @ParameterizedTest
    @MethodSource("repositories")
    void getAllReturnsEmptyCollectionInitially(QuestionRepository repository) {
        assertThat(repository.getAll()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("repositories")
    void addStoresAndReturnsQuestion(QuestionRepository repository) {
        Question question = new Question("Question", "Answer");

        assertThat(repository.add(question)).isSameAs(question);
        assertThat(repository.getAll()).containsExactly(question);
    }

    @ParameterizedTest
    @MethodSource("repositories")
    void duplicatesAreNotStoredTwice(QuestionRepository repository) {
        repository.add(new Question("Question", "Answer"));
        repository.add(new Question("Question", "Answer"));

        assertThat(repository.getAll()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("repositories")
    void differentAnswersAreStoredSeparately(QuestionRepository repository) {
        Question first = new Question("Question", "First answer");
        Question second = new Question("Question", "Second answer");
        repository.add(first);
        repository.add(second);

        assertThat(repository.getAll()).containsExactlyInAnyOrder(first, second);
    }

    @ParameterizedTest
    @MethodSource("repositories")
    void removeDeletesEqualQuestion(QuestionRepository repository) {
        Question question = new Question("Question", "Answer");
        repository.add(question);

        assertThat(repository.remove(new Question("Question", "Answer"))).isEqualTo(question);
        assertThat(repository.getAll()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("repositories")
    void removalFromEmptyRepositoryThrows(QuestionRepository repository) {
        assertThrows(QuestionNotFoundException.class,
                () -> repository.remove(new Question("Missing", "Answer")));
        assertThat(repository.getAll()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("repositories")
    void getAllCannotBeUsedToModifyStoredQuestions(QuestionRepository repository) {
        Question question = new Question("Question", "Answer");
        repository.add(question);
        Collection<Question> questions = repository.getAll();

        assertThrows(UnsupportedOperationException.class, questions::clear);
        assertThat(repository.getAll()).containsExactly(question);
    }

    @ParameterizedTest
    @MethodSource("repositories")
    void getAllReturnsSnapshot(QuestionRepository repository) {
        Question first = new Question("First", "Answer 1");
        Question second = new Question("Second", "Answer 2");
        repository.add(first);
        Collection<Question> snapshot = repository.getAll();

        repository.add(second);

        assertThat(snapshot).containsExactly(first);
        assertThat(repository.getAll()).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void subjectsHaveSeparateQuestionStores() {
        QuestionRepository javaRepository = new JavaQuestionRepository();
        QuestionRepository mathRepository = new MathQuestionRepository();
        javaRepository.add(new Question("Java", "Answer"));

        assertThat(mathRepository.getAll()).isEmpty();
    }
}

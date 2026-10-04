package org.skypro.skyshop.exam.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionTest {
    @Test
    void constructorAndGettersKeepQuestionAndAnswer() {
        Question question = new Question("What is Java?", "A programming language");

        assertThat(question.getQuestion()).isEqualTo("What is Java?");
        assertThat(question.getAnswer()).isEqualTo("A programming language");
    }

    @Test
    void equalQuestionsHaveSameHashCode() {
        Question first = new Question("Question", "Answer");
        Question second = new Question("Question", "Answer");
        Question third = new Question("Question", "Answer");

        assertThat(first).isEqualTo(first).isEqualTo(second);
        assertThat(second).isEqualTo(first).isEqualTo(third);
        assertThat(first).isEqualTo(third);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void equalityUsesBothFieldsAndRejectsOtherClassesAndNull() {
        Question question = new Question("Question", "Answer");

        assertThat(question).isNotEqualTo(new Question("Other", "Answer"));
        assertThat(question).isNotEqualTo(new Question("Question", "Other"));
        assertThat(question.equals(null)).isFalse();
        assertThat(question.equals("Question")).isFalse();
    }

    @Test
    void equalityHandlesNullFields() {
        assertThat(new Question(null, null)).isEqualTo(new Question(null, null));
        assertThat(new Question(null, "Answer")).isEqualTo(new Question(null, "Answer"));
    }
}

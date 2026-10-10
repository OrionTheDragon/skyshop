package org.skypro.skyshop.exam.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skypro.skyshop.exam.model.Question;
import org.skypro.skyshop.exam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExamControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("javaQuestionRepository")
    private QuestionRepository javaRepository;

    @Autowired
    @Qualifier("mathQuestionRepository")
    private QuestionRepository mathRepository;

    @BeforeEach
    void clearQuestionStores() {
        javaRepository.getAll().forEach(javaRepository::remove);
        mathRepository.getAll().forEach(mathRepository::remove);
    }

    @ParameterizedTest
    @ValueSource(strings = {"java", "math"})
    void questionsCanBeAddedListedAndRemoved(String subject) throws Exception {
        String basePath = "/exam/" + subject;
        mockMvc.perform(post(basePath + "/add").param("question", "Question").param("answer", "Answer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("Question"))
                .andExpect(jsonPath("$.answer").value("Answer"));

        mockMvc.perform(post(basePath + "/add").param("question", "Question").param("answer", "Answer"))
                .andExpect(status().isOk());
        mockMvc.perform(get(basePath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete(basePath + "/remove").param("question", "Question").param("answer", "Answer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("Question"));
        mockMvc.perform(get(basePath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"java", "math"})
    void mutationRoutesRequirePostAndDelete(String subject) throws Exception {
        mockMvc.perform(get("/exam/" + subject + "/add").param("question", "Question").param("answer", "Answer"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/exam/" + subject + "/remove").param("question", "Question").param("answer", "Answer"))
                .andExpect(status().isMethodNotAllowed());
    }

    @ParameterizedTest
    @ValueSource(strings = {"java", "math"})
    void removingMissingQuestionReturnsNotFound(String subject) throws Exception {
        mockMvc.perform(delete("/exam/" + subject + "/remove")
                        .param("question", "Missing").param("answer", "Answer"))
                .andExpect(status().isNotFound());
    }

    @Test
    void examUsesBothSubjectsAndLeavesRepositoriesIntact() throws Exception {
        javaRepository.add(new Question("What is Java?", "A programming language"));
        mathRepository.add(new Question("2 + 2", "4"));

        mockMvc.perform(get("/exam/get/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].question", containsInAnyOrder("What is Java?", "2 + 2")))
                .andExpect(jsonPath("$[*].answer", containsInAnyOrder("A programming language", "4")));
        mockMvc.perform(get("/exam/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].question").value("What is Java?"));
        mockMvc.perform(get("/exam/math"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].question").value("2 + 2"));
    }

    @Test
    void zeroQuestionsReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/exam/get/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void requestingTooManyQuestionsReturnsBadRequest() throws Exception {
        javaRepository.add(new Question("What is Java?", "A programming language"));

        mockMvc.perform(get("/exam/get/2")).andExpect(status().isBadRequest());
    }

    @Test
    void negativeQuestionAmountReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/exam/get/-1")).andExpect(status().isBadRequest());
    }
}

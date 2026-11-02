package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(StudentController.class)
class StudentStatisticsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Test
    void countEndpointReturnsNumber() throws Exception {
        when(studentService.getStudentCount()).thenReturn(3L);

        mockMvc.perform(get("/student/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void averageAgeEndpointReturnsNumber() throws Exception {
        when(studentService.getAverageAge()).thenReturn(12.5);

        mockMvc.perform(get("/student/average-age"))
                .andExpect(status().isOk())
                .andExpect(content().string("12.5"));
    }

    @Test
    void lastFiveEndpointReturnsStudents() throws Exception {
        when(studentService.getLastFiveStudents()).thenReturn(List.of(new Student("Harry", 11)));

        mockMvc.perform(get("/student/last-five"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Harry"));
    }
}

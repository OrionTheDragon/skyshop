package ru.hogwarts.school.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.StudentController;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ru.hogwarts.school.exception.StudentNotFoundException;

@WebMvcTest(StudentController.class)
class StudentControllerWebMvcTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @MockBean private StudentService service;

    @Test
    void createStudent() throws Exception {
        Student saved = new Student("Harry", 11);
        saved.setId(1L);
        when(service.addStudent(any(Student.class))).thenReturn(saved);
        mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new Student("Harry", 11))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Harry"));
    }

    @Test
    void getStudentById() throws Exception {
        Student saved = new Student("Harry", 11);
        saved.setId(1L);
        when(service.findStudent(1L)).thenReturn(saved);
        mvc.perform(get("/student/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Harry"))
                .andExpect(jsonPath("$.age").value(11));
    }

    @Test
    void listStudents() throws Exception {
        when(service.getAllStudents()).thenReturn(List.of(new Student("Harry", 11)));
        mvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Harry"));
    }

    @Test
    void filterStudentsByAge() throws Exception {
        when(service.findStudentsByAge(11)).thenReturn(List.of(new Student("Harry", 11)));
        mvc.perform(get("/student").param("age", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].age").value(11));
        verify(service).findStudentsByAge(11);
    }

    @Test
    void filterStudentsByRange() throws Exception {
        when(service.findStudentsByAgeBetween(10, 20)).thenReturn(List.of(new Student("Harry", 11)));
        mvc.perform(get("/student").param("min", "10").param("max", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Harry"));
        verify(service).findStudentsByAgeBetween(10, 20);
    }

    @Test
    void getStudentsFaculty() throws Exception {
        Faculty faculty = new Faculty("Gryffindor", "Red");
        faculty.setId(2L);
        when(service.getStudentFaculty(1L)).thenReturn(faculty);
        mvc.perform(get("/student/{id}/faculty", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Gryffindor"));
    }

    @Test
    void updateStudent() throws Exception {
        Student updated = new Student("Hermione", 12);
        updated.setId(1L);
        when(service.editStudent(any(Student.class))).thenReturn(updated);
        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Hermione"));
    }

    @Test
    void deleteStudent() throws Exception {
        mvc.perform(delete("/student/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).deleteStudent(1L);
    }

    @Test
    void missingStudentReturns404() throws Exception {
        when(service.findStudent(99L)).thenThrow(new StudentNotFoundException(99L));
        mvc.perform(get("/student/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student with id 99 not found"));
    }
}

package ru.hogwarts.school.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.FacultyController;
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

import ru.hogwarts.school.exception.FacultyNotFoundException;

@WebMvcTest(FacultyController.class)
class FacultyControllerWebMvcTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @MockBean private FacultyService service;

    @Test
    void createFaculty() throws Exception {
        Faculty saved = new Faculty("Gryffindor", "Red");
        saved.setId(1L);
        when(service.addFaculty(any(Faculty.class))).thenReturn(saved);
        mvc.perform(post("/faculty").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new Faculty("Gryffindor", "Red"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gryffindor"));
    }

    @Test
    void getFacultyById() throws Exception {
        Faculty saved = new Faculty("Gryffindor", "Red");
        saved.setId(1L);
        when(service.findFaculty(1L)).thenReturn(saved);
        mvc.perform(get("/faculty/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("Red"));
    }

    @Test
    void listFaculties() throws Exception {
        when(service.getAllFaculties()).thenReturn(List.of(new Faculty("Gryffindor", "Red")));
        mvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Gryffindor"));
    }

    @Test
    void filterFacultiesByColor() throws Exception {
        when(service.findFacultiesByColor("rEd")).thenReturn(List.of(new Faculty("Gryffindor", "Red")));
        mvc.perform(get("/faculty").param("color", "rEd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].color").value("Red"));
        verify(service).findFacultiesByColor("rEd");
    }

    @Test
    void filterFacultiesByNameOrColor() throws Exception {
        when(service.findFacultiesByNameOrColor("ffIN")).thenReturn(List.of(new Faculty("Gryffindor", "Red")));
        mvc.perform(get("/faculty").param("pattern", "ffIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Gryffindor"));
        verify(service).findFacultiesByNameOrColor("ffIN");
    }

    @Test
    void getFacultyStudents() throws Exception {
        when(service.getFacultyStudents(1L)).thenReturn(List.of(new Student("Harry", 11)));
        mvc.perform(get("/faculty/{id}/students", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Harry"));
    }

    @Test
    void updateFaculty() throws Exception {
        Faculty updated = new Faculty("Gryffindor", "Gold");
        updated.setId(1L);
        when(service.editFaculty(any(Faculty.class))).thenReturn(updated);
        mvc.perform(put("/faculty").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.color").value("Gold"));
    }

    @Test
    void deleteFaculty() throws Exception {
        mvc.perform(delete("/faculty/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).deleteFaculty(1L);
    }

    @Test
    void missingFacultyReturns404() throws Exception {
        when(service.findFaculty(99L)).thenThrow(new FacultyNotFoundException(99L));
        mvc.perform(get("/faculty/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Faculty with id 99 not found"));
    }
}

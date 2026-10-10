package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SchoolSqlControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StudentRepository students;
    @Autowired
    private FacultyRepository faculties;

    @AfterEach
    void clearTestDatabase() {
        students.deleteAll();
        faculties.deleteAll();
    }

    @Test
    void ageRangeIncludesBothBoundariesAndReturnsEmptyForNoMatches() throws Exception {
        students.save(new Student("Younger", 9));
        students.save(new Student("Lower boundary", 10));
        students.save(new Student("Upper boundary", 20));
        students.save(new Student("Older", 21));

        mvc.perform(get("/student").param("min", "10").param("max", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/student").param("min", "20").param("max", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Upper boundary"))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/student").param("min", "30").param("max", "40"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @ParameterizedTest
    @CsvSource(value = {"10,NULL", "NULL,20", "20,10", "text,20"}, nullValues = "NULL")
    void invalidAgeRangeReturns400(String min, String max) throws Exception {
        var request = get("/student");
        if (min != null) {
            request.param("min", min);
        }
        if (max != null) {
            request.param("max", max);
        }
        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    void rangeCannotBeCombinedWithExactAge() throws Exception {
        mvc.perform(get("/student").param("age", "11").param("min", "10").param("max", "20"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void facultySearchMatchesPartOfNameOrColorIgnoringCase() throws Exception {
        faculties.save(new Faculty("Gryffindor", "Red"));
        faculties.save(new Faculty("Red House", "RED"));
        faculties.save(new Faculty("Slytherin", "Green"));

        mvc.perform(get("/faculty").param("pattern", "rEd"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/faculty").param("pattern", "FFIN"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Gryffindor"))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/faculty").param("pattern", "unknown"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void studentFacultyIsPersistedAndCanBeReadInBothDirections() throws Exception {
        Faculty faculty = faculties.save(new Faculty("Gryffindor", "Red"));
        Student request = new Student("Harry", 11);
        request.setFaculty(faculty);
        String response = mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.faculty.id").value(faculty.getId()))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();

        mvc.perform(get("/student/" + id + "/faculty"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(faculty.getId()))
                .andExpect(jsonPath("$.students").doesNotExist());
        mvc.perform(get("/faculty/" + faculty.getId() + "/students"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].faculty.students").doesNotExist());

        mvc.perform(delete("/faculty/" + faculty.getId())).andExpect(status().isConflict());
        mvc.perform(delete("/student/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/faculty/" + faculty.getId() + "/students"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/faculty/" + faculty.getId())).andExpect(status().isOk());
    }

    @Test
    void updateReassignsAndClearsStudentFaculty() throws Exception {
        Faculty first = faculties.save(new Faculty("Gryffindor", "Red"));
        Faculty second = faculties.save(new Faculty("Slytherin", "Green"));
        Student student = new Student("Harry", 11);
        student.setFaculty(first);
        student = students.save(student);
        student.setFaculty(second);

        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.faculty.id").value(second.getId()));
        mvc.perform(get("/faculty/" + first.getId() + "/students"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/faculty/" + second.getId() + "/students"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));

        student.setFaculty(null);
        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk());
        mvc.perform(get("/student/" + student.getId() + "/faculty")).andExpect(status().isNotFound());
        mvc.perform(get("/faculty/" + second.getId() + "/students"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void missingRelationsReturn404AndEmptyFacultyReturnsEmptyList() throws Exception {
        Student student = students.save(new Student("Harry", 11));
        Faculty faculty = faculties.save(new Faculty("Gryffindor", "Red"));

        mvc.perform(get("/student/" + student.getId() + "/faculty")).andExpect(status().isNotFound());
        mvc.perform(get("/student/" + Long.MAX_VALUE + "/faculty")).andExpect(status().isNotFound());
        mvc.perform(get("/faculty/" + Long.MAX_VALUE + "/students")).andExpect(status().isNotFound());
        mvc.perform(get("/faculty/" + faculty.getId() + "/students"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void invalidFacultyDoesNotCreateStudent() throws Exception {
        Student student = new Student("Harry", 11);
        Faculty faculty = new Faculty("Unknown", "Red");
        faculty.setId(Long.MAX_VALUE);
        student.setFaculty(faculty);

        mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isNotFound());
        faculty.setId(null);
        mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isBadRequest());
        assertThat(students.findAll()).isEmpty();
        assertThat(faculties.findAll()).isEmpty();
    }
}

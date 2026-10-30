package ru.hogwarts.school.test.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FacultyControllerRestTemplateTest {
    @Autowired private TestRestTemplate rest;
    @Autowired private StudentRepository students;
    @Autowired private FacultyRepository faculties;

    @BeforeEach
    @AfterEach
    void clearTestData() {
        students.deleteAll();
        faculties.deleteAll();
    }

    @Test
    void createFaculty() {
        ResponseEntity<Faculty> response = rest.postForEntity("/faculty",
                new Faculty("Gryffindor", "Red"), Faculty.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Gryffindor", response.getBody().getName());
    }

    @Test
    void getFacultyById() {
        Faculty saved = faculties.save(new Faculty("Gryffindor", "Red"));
        ResponseEntity<Faculty> response = rest.getForEntity("/faculty/{id}", Faculty.class, saved.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(saved.getId(), response.getBody().getId());
        assertEquals("Red", response.getBody().getColor());
    }

    @Test
    void listFaculties() {
        faculties.save(new Faculty("Gryffindor", "Red"));
        ResponseEntity<List<Faculty>> response = rest.exchange("/faculty", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Gryffindor", response.getBody().get(0).getName());
    }

    @Test
    void filterFacultiesByColor() {
        faculties.save(new Faculty("Gryffindor", "Red"));
        faculties.save(new Faculty("Slytherin", "Green"));
        ResponseEntity<List<Faculty>> response = rest.exchange("/faculty?color=rEd", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Gryffindor", response.getBody().get(0).getName());
    }

    @Test
    void filterFacultiesByNameOrColor() {
        faculties.save(new Faculty("Gryffindor", "Red"));
        faculties.save(new Faculty("Slytherin", "Green"));
        ResponseEntity<List<Faculty>> response = rest.exchange("/faculty?pattern=ffIN", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Gryffindor", response.getBody().get(0).getName());
    }

    @Test
    void getFacultyStudents() {
        Faculty faculty = faculties.save(new Faculty("Gryffindor", "Red"));
        Student student = new Student("Harry", 11);
        student.setFaculty(faculty);
        students.save(student);
        ResponseEntity<List<Student>> response = rest.exchange("/faculty/{id}/students",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {}, faculty.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Harry", response.getBody().get(0).getName());
    }

    @Test
    void updateFaculty() {
        Faculty faculty = faculties.save(new Faculty("Gryffindor", "Red"));
        faculty.setColor("Gold");
        ResponseEntity<Faculty> response = rest.exchange("/faculty", HttpMethod.PUT,
                new HttpEntity<>(faculty), Faculty.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Gold", response.getBody().getColor());
    }

    @Test
    void deleteFaculty() {
        Faculty faculty = faculties.save(new Faculty("Gryffindor", "Red"));
        ResponseEntity<String> response = rest.exchange("/faculty/{id}", HttpMethod.DELETE,
                null, String.class, faculty.getId());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        assertFalse(faculties.existsById(faculty.getId()));
    }

    @Test
    void missingFacultyReturns404() {
        ResponseEntity<String> response = rest.getForEntity("/faculty/{id}", String.class, Long.MAX_VALUE);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Faculty with id " + Long.MAX_VALUE + " not found"));
    }
}

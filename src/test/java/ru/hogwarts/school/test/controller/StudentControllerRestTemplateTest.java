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
class StudentControllerRestTemplateTest {
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
    void createStudent() {
        ResponseEntity<Student> response = rest.postForEntity("/student", new Student("Harry", 11), Student.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Harry", response.getBody().getName());
    }

    @Test
    void getStudentById() {
        Student saved = students.save(new Student("Harry", 11));
        ResponseEntity<Student> response = rest.getForEntity("/student/{id}", Student.class, saved.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(saved.getId(), response.getBody().getId());
        assertEquals(11, response.getBody().getAge());
    }

    @Test
    void listStudents() {
        students.save(new Student("Harry", 11));
        ResponseEntity<List<Student>> response = rest.exchange("/student", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Harry", response.getBody().get(0).getName());
    }

    @Test
    void filterStudentsByAge() {
        students.save(new Student("Harry", 11));
        students.save(new Student("Cedric", 14));
        ResponseEntity<List<Student>> response = rest.exchange("/student?age=11", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Harry", response.getBody().get(0).getName());
    }

    @Test
    void filterStudentsByAgeRange() {
        students.save(new Student("Harry", 10));
        students.save(new Student("Cedric", 20));
        students.save(new Student("Older", 21));
        ResponseEntity<List<Student>> response = rest.exchange("/student?min=10&max=20", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream().allMatch(student -> student.getAge() >= 10 && student.getAge() <= 20));
    }

    @Test
    void getStudentsFaculty() {
        Faculty faculty = faculties.save(new Faculty("Gryffindor", "Red"));
        Student student = new Student("Harry", 11);
        student.setFaculty(faculty);
        student = students.save(student);
        ResponseEntity<Faculty> response = rest.getForEntity("/student/{id}/faculty", Faculty.class, student.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(faculty.getId(), response.getBody().getId());
    }

    @Test
    void updateStudent() {
        Student student = students.save(new Student("Harry", 11));
        student.setName("Hermione");
        student.setAge(12);
        ResponseEntity<Student> response = rest.exchange("/student", HttpMethod.PUT,
                new HttpEntity<>(student), Student.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Hermione", response.getBody().getName());
        assertEquals(12, response.getBody().getAge());
    }

    @Test
    void deleteStudent() {
        Student student = students.save(new Student("Harry", 11));
        ResponseEntity<String> response = rest.exchange("/student/{id}", HttpMethod.DELETE,
                null, String.class, student.getId());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        assertFalse(students.existsById(student.getId()));
    }

    @Test
    void missingStudentReturns404() {
        ResponseEntity<String> response = rest.getForEntity("/student/{id}", String.class, Long.MAX_VALUE);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Student with id " + Long.MAX_VALUE + " not found"));
    }
}

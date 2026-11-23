package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class StudentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    @Transactional
    public Student addStudent(Student student) {
        LOGGER.info("Was invoked method for adding a student");
        student.setId(null);
        student.setFaculty(resolveFaculty(student.getFaculty()));
        return studentRepository.save(student);
    }

    public Student findStudent(Long id) {
        LOGGER.info("Was invoked method for finding a student with id={}", id);
        return studentRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("No student with id={}", id);
                    return new StudentNotFoundException(id);
                });
    }

    public List<Student> getAllStudents() {
        LOGGER.info("Was invoked method for getting all students");
        return studentRepository.findAll();
    }

    public List<String> getNamesStartingWithA() {
        LOGGER.info("Was invoked method for finding student names starting with A");
        return studentRepository.findAll().stream()
                .map(Student::getName)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .filter(name -> name.startsWith("A") || name.startsWith("А"))
                .sorted()
                .toList();
    }

    public double getAverageAgeUsingStream() {
        LOGGER.info("Was invoked method for calculating the average student age using a stream");
        return studentRepository.findAll().stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0);
    }

    public long getStudentCount() {
        LOGGER.info("Was invoked method for counting all students");
        return studentRepository.countAllStudents();
    }

    public double getAverageAge() {
        LOGGER.info("Was invoked method for calculating the average student age");
        Double averageAge = studentRepository.findAverageAge();
        if (averageAge == null) {
            LOGGER.warn("No students found when calculating the average age");
            return 0.0;
        }
        return averageAge;
    }

    public List<Student> getLastFiveStudents() {
        LOGGER.info("Was invoked method for getting the last five students");
        return studentRepository.findLastFiveStudents();
    }

    public List<Student> findStudentsByAge(int age) {
        LOGGER.info("Was invoked method for finding students with age={}", age);
        return studentRepository.findByAge(age);
    }

    public List<Student> findStudentsByAgeBetween(int min, int max) {
        LOGGER.info("Was invoked method for finding students with ages between {} and {}", min, max);
        if (min > max) {
            LOGGER.error("Invalid student age range with min={} and max={}", min, max);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min must not exceed max");
        }
        return studentRepository.findByAgeBetween(min, max);
    }

    public Faculty getStudentFaculty(Long id) {
        LOGGER.info("Was invoked method for getting the faculty of student with id={}", id);
        Faculty faculty = findStudent(id).getFaculty();
        if (faculty == null) {
            LOGGER.error("No faculty assigned to student with id={}", id);
            throw new FacultyNotFoundException("Faculty for student with id " + id + " not found");
        }
        return faculty;
    }

    @Transactional
    public Student editStudent(Student student) {
        LOGGER.info("Was invoked method for editing a student with id={}", student.getId());
        Student existing = findStudent(student.getId());
        existing.setName(student.getName());
        existing.setAge(student.getAge());
        existing.setFaculty(resolveFaculty(student.getFaculty()));
        return studentRepository.save(existing);
    }

    @Transactional
    public void deleteStudent(Long id) {
        LOGGER.info("Was invoked method for deleting a student with id={}", id);
        studentRepository.delete(findStudent(id));
    }

    private Faculty resolveFaculty(Faculty faculty) {
        LOGGER.debug("Was invoked method for resolving a student faculty");
        if (faculty == null) {
            return null;
        }
        Long facultyId = faculty.getId();
        if (facultyId == null) {
            LOGGER.error("Cannot assign a faculty without a faculty id");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty id is required");
        }
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> {
                    LOGGER.error("No faculty with id={} to assign to the student", facultyId);
                    return new FacultyNotFoundException(facultyId);
                });
    }
}

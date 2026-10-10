package ru.hogwarts.school.service;

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

@Service
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    @Transactional
    public Student addStudent(Student student) {
        student.setId(null);
        student.setFaculty(resolveFaculty(student.getFaculty()));
        return studentRepository.save(student);
    }

    public Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> findStudentsByAge(int age) {
        return studentRepository.findByAge(age);
    }

    public List<Student> findStudentsByAgeBetween(int min, int max) {
        if (min > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min must not exceed max");
        }
        return studentRepository.findByAgeBetween(min, max);
    }

    public Faculty getStudentFaculty(Long id) {
        Faculty faculty = findStudent(id).getFaculty();
        if (faculty == null) {
            throw new FacultyNotFoundException("Faculty for student with id " + id + " not found");
        }
        return faculty;
    }

    @Transactional
    public Student editStudent(Student student) {
        Student existing = findStudent(student.getId());
        existing.setName(student.getName());
        existing.setAge(student.getAge());
        existing.setFaculty(resolveFaculty(student.getFaculty()));
        return studentRepository.save(existing);
    }

    @Transactional
    public void deleteStudent(Long id) {
        studentRepository.delete(findStudent(id));
    }

    private Faculty resolveFaculty(Faculty faculty) {
        if (faculty == null) {
            return null;
        }
        if (faculty.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty id is required");
        }
        return facultyRepository.findById(faculty.getId())
                .orElseThrow(() -> new FacultyNotFoundException(faculty.getId()));
    }
}

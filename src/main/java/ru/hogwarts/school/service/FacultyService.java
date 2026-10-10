package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FacultyService {
    private final FacultyRepository facultyRepository;

    @Autowired
    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    @Transactional
    public Faculty addFaculty(Faculty faculty) {
        faculty.setId(null);
        return facultyRepository.save(faculty);
    }

    public Faculty findFaculty(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException(id));
    }

    public List<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    public List<Faculty> findFacultiesByColor(String color) {
        return facultyRepository.findByColorIgnoreCase(color);
    }

    public List<Faculty> findFacultiesByNameOrColor(String pattern) {
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(pattern, pattern);
    }

    public List<Student> getFacultyStudents(Long id) {
        return List.copyOf(findFaculty(id).getStudents());
    }

    @Transactional
    public Faculty editFaculty(Faculty faculty) {
        Faculty existing = findFaculty(faculty.getId());
        existing.setName(faculty.getName());
        existing.setColor(faculty.getColor());
        return facultyRepository.save(existing);
    }

    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = findFaculty(id);
        if (!faculty.getStudents().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Faculty still has students");
        }
        facultyRepository.delete(faculty);
    }
}

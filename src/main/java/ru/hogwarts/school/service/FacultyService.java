package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FacultyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacultyService.class);

    private final FacultyRepository facultyRepository;

    @Autowired
    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    @Transactional
    public Faculty addFaculty(Faculty faculty) {
        LOGGER.info("Was invoked method for adding a faculty");
        faculty.setId(null);
        return facultyRepository.save(faculty);
    }

    public Faculty findFaculty(Long id) {
        LOGGER.info("Was invoked method for finding a faculty with id={}", id);
        return facultyRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("No faculty with id={}", id);
                    return new FacultyNotFoundException(id);
                });
    }

    public List<Faculty> getAllFaculties() {
        LOGGER.info("Was invoked method for getting all faculties");
        return facultyRepository.findAll();
    }

    public Optional<String> getLongestFacultyName() {
        LOGGER.info("Was invoked method for finding the longest faculty name");
        return facultyRepository.findAll().stream()
                .map(Faculty::getName)
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(String::length));
    }

    public List<Faculty> findFacultiesByColor(String color) {
        LOGGER.info("Was invoked method for filtering faculties by color");
        return facultyRepository.findByColorIgnoreCase(color);
    }

    public List<Faculty> findFacultiesByNameOrColor(String pattern) {
        LOGGER.info("Was invoked method for filtering faculties by name or color");
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(pattern, pattern);
    }

    public List<Student> getFacultyStudents(Long id) {
        LOGGER.info("Was invoked method for getting students of faculty with id={}", id);
        return List.copyOf(findFaculty(id).getStudents());
    }

    @Transactional
    public Faculty editFaculty(Faculty faculty) {
        LOGGER.info("Was invoked method for editing a faculty with id={}", faculty.getId());
        Faculty existing = findFaculty(faculty.getId());
        existing.setName(faculty.getName());
        existing.setColor(faculty.getColor());
        return facultyRepository.save(existing);
    }

    @Transactional
    public void deleteFaculty(Long id) {
        LOGGER.info("Was invoked method for deleting a faculty with id={}", id);
        Faculty faculty = findFaculty(id);
        if (!faculty.getStudents().isEmpty()) {
            LOGGER.error("Cannot delete faculty with id={} because it still has students", id);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Faculty still has students");
        }
        facultyRepository.delete(faculty);
    }
}

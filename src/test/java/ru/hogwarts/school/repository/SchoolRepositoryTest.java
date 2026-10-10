package ru.hogwarts.school.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SchoolRepositoryTest {
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private FacultyRepository facultyRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void studentCrudPersistsAndGeneratesIds() {
        Student student = studentRepository.saveAndFlush(new Student("Harry", 11));
        Long id = student.getId();
        assertThat(id).isPositive();
        entityManager.clear();

        Student loaded = studentRepository.findById(id).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Harry");
        loaded.setAge(12);
        studentRepository.saveAndFlush(loaded);
        entityManager.clear();
        assertThat(studentRepository.findById(id).orElseThrow().getAge()).isEqualTo(12);

        studentRepository.deleteById(id);
        studentRepository.flush();
        assertThat(studentRepository.findById(id)).isEmpty();
    }

    @Test
    void studentAgeQueryFiltersInDatabase() {
        Student harry = studentRepository.saveAndFlush(new Student("Harry", 11));
        studentRepository.saveAndFlush(new Student("Cedric", 14));
        entityManager.clear();

        assertThat(studentRepository.findByAge(11))
                .extracting(Student::getId).containsExactly(harry.getId());
        assertThat(studentRepository.findByAge(100)).isEmpty();
    }

    @Test
    void facultyCrudPersistsAndGeneratesIds() {
        Faculty faculty = facultyRepository.saveAndFlush(new Faculty("Gryffindor", "Red"));
        Long id = faculty.getId();
        assertThat(id).isPositive();
        entityManager.clear();

        Faculty loaded = facultyRepository.findById(id).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Gryffindor");
        loaded.setColor("Gold");
        facultyRepository.saveAndFlush(loaded);
        entityManager.clear();
        assertThat(facultyRepository.findById(id).orElseThrow().getColor()).isEqualTo("Gold");

        facultyRepository.deleteById(id);
        facultyRepository.flush();
        assertThat(facultyRepository.findById(id)).isEmpty();
    }

    @Test
    void facultyColorQueryIgnoresCaseAndFiltersInDatabase() {
        Faculty gryffindor = facultyRepository.saveAndFlush(new Faculty("Gryffindor", "Red"));
        facultyRepository.saveAndFlush(new Faculty("Slytherin", "Green"));
        entityManager.clear();

        assertThat(facultyRepository.findByColorIgnoreCase("rED"))
                .extracting(Faculty::getId).containsExactly(gryffindor.getId());
        assertThat(facultyRepository.findByColorIgnoreCase("Blue")).isEmpty();
    }

    @Test
    void ageRangeQueryIncludesItsBoundaries() {
        Student lower = studentRepository.save(new Student("Harry", 10));
        Student upper = studentRepository.save(new Student("Hermione", 20));
        studentRepository.save(new Student("Younger", 9));
        studentRepository.save(new Student("Older", 21));
        studentRepository.flush();
        entityManager.clear();

        assertThat(studentRepository.findByAgeBetween(10, 20))
                .extracting(Student::getId).containsExactlyInAnyOrder(lower.getId(), upper.getId());
        assertThat(studentRepository.findByAgeBetween(20, 20))
                .extracting(Student::getId).containsExactly(upper.getId());
        assertThat(studentRepository.findByAgeBetween(30, 40)).isEmpty();
    }

    @Test
    void facultyNameOrColorQueryMatchesSubstringsIgnoringCaseWithoutDuplicates() {
        Faculty colorMatch = facultyRepository.save(new Faculty("Gryffindor", "Red"));
        Faculty bothMatch = facultyRepository.save(new Faculty("Red House", "RED"));
        facultyRepository.save(new Faculty("Slytherin", "Green"));
        facultyRepository.flush();
        entityManager.clear();

        assertThat(facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase("rEd", "rEd"))
                .extracting(Faculty::getId).containsExactlyInAnyOrder(colorMatch.getId(), bothMatch.getId());
        assertThat(facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase("FFIN", "FFIN"))
                .extracting(Faculty::getId).containsExactly(colorMatch.getId());
        assertThat(facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase("unknown", "unknown"))
                .isEmpty();
    }

    @Test
    void facultyRelationshipSurvivesClearingPersistenceContext() {
        Faculty faculty = facultyRepository.save(new Faculty("Gryffindor", "Red"));
        Student student = new Student("Harry", 11);
        student.setFaculty(faculty);
        studentRepository.saveAndFlush(student);
        entityManager.clear();

        assertThat(studentRepository.findById(student.getId()).orElseThrow().getFaculty().getId())
                .isEqualTo(faculty.getId());
        assertThat(facultyRepository.findById(faculty.getId()).orElseThrow().getStudents())
                .extracting(Student::getId).containsExactly(student.getId());
    }
}

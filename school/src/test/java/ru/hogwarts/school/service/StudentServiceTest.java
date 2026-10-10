package ru.hogwarts.school.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hogwarts.school.exception.EntityNotFoundException;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock
    private StudentRepository repository;
    @InjectMocks
    private StudentService service;

    @Test
    void addUsesRepositoryAndDatabaseGeneratedId() {
        Student entity = new Student("Harry", 11);
        entity.setId(99L);
        when(repository.save(entity)).thenReturn(entity);

        assertThat(service.addStudent(entity)).isSameAs(entity);
        assertThat(entity.getId()).isNull();
        verify(repository).save(entity);
    }

    @Test
    void findReturnsRepositoryEntity() {
        Student entity = new Student("Harry", 11);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertThat(service.findStudent(1L)).isSameAs(entity);
    }

    @Test
    void findMissingEntityThrowsNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findStudent(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getAllUsesRepository() {
        Student entity = new Student("Harry", 11);
        when(repository.findAll()).thenReturn(List.of(entity));

        assertThat(service.getAllStudents()).containsExactly(entity);
        verify(repository).findAll();
    }

    @Test
    void filterUsesDerivedRepositoryQuery() {
        Student entity = new Student("Harry", 11);
        when(repository.findByAge(11)).thenReturn(List.of(entity));

        assertThat(service.findStudentsByAge(11)).containsExactly(entity);
        verify(repository).findByAge(11);
    }

    @Test
    void filterWithoutMatchesReturnsEmptyList() {
        when(repository.findByAge(12)).thenReturn(List.of());

        assertThat(service.findStudentsByAge(12)).isEmpty();
    }

    @Test
    void editUpdatesExistingEntityInRepository() {
        Student existing = new Student("Harry", 11);
        existing.setId(1L);
        Student updated = new Student("Hermione", 12);
        updated.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertThat(service.editStudent(updated)).isSameAs(existing);
        assertThat(existing.getId()).isEqualTo(1L);
        assertThat(existing.getName()).isEqualTo(updated.getName());
        assertThat(existing.getAge()).isEqualTo(12);
        verify(repository).save(existing);
    }

    @Test
    void editMissingEntityDoesNotInsertIt() {
        Student updated = new Student("Hermione", 12);
        updated.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editStudent(updated))
                .isInstanceOf(EntityNotFoundException.class);
        verify(repository, never()).save(updated);
    }

    @Test
    void deleteUsesRepository() {
        Student entity = new Student("Harry", 11);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.deleteStudent(1L);

        verify(repository).delete(entity);
    }

    @Test
    void deleteMissingEntityThrowsNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteStudent(1L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(repository, never()).deleteById(1L);
    }
}

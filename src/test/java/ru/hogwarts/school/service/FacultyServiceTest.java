package ru.hogwarts.school.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacultyServiceTest {
    @Mock
    private FacultyRepository repository;
    @InjectMocks
    private FacultyService service;

    @Test
    void addUsesRepositoryAndDatabaseGeneratedId() {
        Faculty entity = new Faculty("Gryffindor", "Red");
        entity.setId(99L);
        when(repository.save(entity)).thenReturn(entity);

        assertThat(service.addFaculty(entity)).isSameAs(entity);
        assertThat(entity.getId()).isNull();
        verify(repository).save(entity);
    }

    @Test
    void findReturnsRepositoryEntity() {
        Faculty entity = new Faculty("Gryffindor", "Red");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertThat(service.findFaculty(1L)).isSameAs(entity);
    }

    @Test
    void findMissingEntityThrowsNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findFaculty(1L))
                .isInstanceOf(FacultyNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getAllUsesRepository() {
        Faculty entity = new Faculty("Gryffindor", "Red");
        when(repository.findAll()).thenReturn(List.of(entity));

        assertThat(service.getAllFaculties()).containsExactly(entity);
        verify(repository).findAll();
    }

    @Test
    void filterUsesDerivedRepositoryQuery() {
        Faculty entity = new Faculty("Gryffindor", "Red");
        when(repository.findByColorIgnoreCase("Red")).thenReturn(List.of(entity));

        assertThat(service.findFacultiesByColor("Red")).containsExactly(entity);
        verify(repository).findByColorIgnoreCase("Red");
    }

    @Test
    void filterWithoutMatchesReturnsEmptyList() {
        when(repository.findByColorIgnoreCase("Blue")).thenReturn(List.of());

        assertThat(service.findFacultiesByColor("Blue")).isEmpty();
    }

    @Test
    void editUpdatesExistingEntityInRepository() {
        Faculty existing = new Faculty("Gryffindor", "Red");
        existing.setId(1L);
        Faculty updated = new Faculty("Ravenclaw", "Blue");
        updated.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertThat(service.editFaculty(updated)).isSameAs(existing);
        assertThat(existing.getId()).isEqualTo(1L);
        assertThat(existing.getName()).isEqualTo(updated.getName());
        assertThat(existing.getColor()).isEqualTo("Blue");
        verify(repository).save(existing);
    }

    @Test
    void editMissingEntityDoesNotInsertIt() {
        Faculty updated = new Faculty("Ravenclaw", "Blue");
        updated.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editFaculty(updated))
                .isInstanceOf(FacultyNotFoundException.class);
        verify(repository, never()).save(updated);
    }

    @Test
    void deleteUsesRepository() {
        Faculty entity = new Faculty("Gryffindor", "Red");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.deleteFaculty(1L);

        verify(repository).delete(entity);
    }

    @Test
    void deleteMissingEntityThrowsNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteFaculty(1L))
                .isInstanceOf(FacultyNotFoundException.class);
        verify(repository, never()).deleteById(1L);
    }
}

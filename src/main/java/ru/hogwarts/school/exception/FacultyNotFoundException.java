package ru.hogwarts.school.exception;

public class FacultyNotFoundException extends RuntimeException {
    public FacultyNotFoundException(Long id) {
        super("Faculty with id " + id + " not found");
    }

    public FacultyNotFoundException(String message) {
        super(message);
    }
}

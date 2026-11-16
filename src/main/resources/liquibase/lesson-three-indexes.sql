--liquibase formatted sql

--changeset school:student-name-index
CREATE INDEX idx_student_name ON students (name);
--rollback DROP INDEX idx_student_name;

--changeset school:faculty-name-color-index
CREATE INDEX idx_faculty_name_color ON faculties (name, color);
--rollback DROP INDEX idx_faculty_name_color;

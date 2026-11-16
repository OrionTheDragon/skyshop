BEGIN;

ALTER TABLE students
    ADD CONSTRAINT chk_students_min_age CHECK (age >= 16),
    ALTER COLUMN age SET DEFAULT 20,
    ALTER COLUMN name SET NOT NULL,
    ADD CONSTRAINT uq_students_name UNIQUE (name);

ALTER TABLE faculties
    ADD CONSTRAINT uq_faculties_name_color UNIQUE (name, color);

COMMIT;

SELECT s.name, s.age, f.name AS faculty_name
FROM students s
LEFT JOIN faculties f ON f.id = s.faculty_id;

SELECT s.name
FROM students s
INNER JOIN avatars a ON a.student_id = s.id;

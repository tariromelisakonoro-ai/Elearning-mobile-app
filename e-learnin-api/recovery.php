<?php
header('Content-Type: text/plain');
include "db.php";

echo "--- LearnExpress Data Recovery & Sync ---\n\n";

// 1. Migrate from 'items' if it exists
$checkItems = $conn->query("SHOW TABLES LIKE 'items'");
if ($checkItems->num_rows > 0) {
    echo "Found old 'items' table. Migrating data...\n";

    // Migrate Exams
    $conn->query("INSERT INTO exams (title, description, course_title, instructor_name, file_url)
                  SELECT title, description, course_title, instructor_name, file_url
                  FROM items WHERE type = 'exam'");

    // Migrate Resources
    $conn->query("INSERT INTO resources (title, description, course_title, instructor_name, file_url)
                  SELECT title, description, course_title, instructor_name, file_url
                  FROM items WHERE type = 'resource'");

    echo "Migration complete.\n";
}

// 2. Ensure Sample Data exists if tables are empty
function populateIfEmpty($conn, $table, $sql) {
    $res = $conn->query("SELECT id FROM $table LIMIT 1");
    if ($res && $res->num_rows == 0) {
        if ($conn->query($sql)) {
            echo "Populated empty table '$table' with sample data.\n";
        } else {
            echo "Error populating '$table': " . $conn->error . "\n";
        }
    } else {
        echo "Table '$table' already has data.\n";
    }
}

populateIfEmpty($conn, 'courses', "INSERT INTO courses (title, description, category, instructor_name) VALUES
    ('Advanced Mathematics', 'Calculus and Algebra', 'Mathematics', 'Dr. Smith'),
    ('Web Development', 'HTML, CSS, and JS', 'Technology', 'Prof. Jones')");

populateIfEmpty($conn, 'assignments', "INSERT INTO assignments (title, description, course_title, instructor_name, due_date) VALUES
    ('Logic Quiz 1', 'Solve the first 10 problems.', 'Mathematics', 'Dr. Smith', '2024-12-01')");

populateIfEmpty($conn, 'community_questions', "INSERT INTO community_questions (title, subject, body, author) VALUES
    ('How to solve Quadratic Equations?', 'Mathematics', 'Can someone explain the formula?', 'Student_User')");

echo "\n--- Sync Finished ---\nRefresh your app now.";
$conn->close();
?>

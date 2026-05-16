<?php
header('Content-Type: text/plain');
include "db.php";

echo "--- LearnExpress Database Refactoring ---\n\n";

// 1. Delete the old 'items' or 'materials' table if they exist
$conn->query("DROP TABLE IF EXISTS items");
$conn->query("DROP TABLE IF EXISTS materials");
echo "Cleaned up old tables.\n";

// 2. Create Exams table
$sql = "CREATE TABLE IF NOT EXISTS exams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    course_title VARCHAR(255),
    instructor_name VARCHAR(100),
    due_date DATETIME,
    file_url VARCHAR(255)
)";
if ($conn->query($sql)) echo "Table 'exams' created.\n";

// 3. Create Quizzes table
$sql = "CREATE TABLE IF NOT EXISTS quizzes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    course_title VARCHAR(255),
    instructor_name VARCHAR(100),
    due_date DATETIME,
    file_url VARCHAR(255)
)";
if ($conn->query($sql)) echo "Table 'quizzes' created.\n";

// 4. Create Resources table
$sql = "CREATE TABLE IF NOT EXISTS resources (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    course_title VARCHAR(255),
    instructor_name VARCHAR(100),
    file_url VARCHAR(255)
)";
if ($conn->query($sql)) echo "Table 'resources' created.\n";

// 5. Ensure Community Questions table matches user's XAMPP
$sql = "CREATE TABLE IF NOT EXISTS community_questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    subject VARCHAR(100),
    body TEXT NOT NULL,
    upvotes INT DEFAULT 0,
    author VARCHAR(100) DEFAULT 'Anonymous',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)";
if ($conn->query($sql)) echo "Table 'community_questions' ready.\n";

echo "\nRefactoring complete! Now update your PHP scripts to fetch from these tables.";
$conn->close();
?>

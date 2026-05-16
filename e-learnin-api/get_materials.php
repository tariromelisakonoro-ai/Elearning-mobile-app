<?php
error_reporting(0);
header('Content-Type: application/json');
include "db.php";

$type = isset($_GET['type']) ? $_GET['type'] : '';

// Map the request type to the specific new tables
if ($type == 'exam') {
    $table = "exams";
} else if ($type == 'quiz') {
    $table = "quizzes";
} else if ($type == 'resource') {
    $table = "resources";
} else {
    // Default to a combined view if no type specified, or handle error
    echo json_encode(["success" => true, "items" => []]);
    exit;
}

$sql = "SELECT * FROM $table";
$result = $conn->query($sql);
$items = array();

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $items[] = array(
            "id" => isset($row['id']) ? $row['id'] : "",
            "type" => $type,
            "title" => isset($row['title']) ? $row['title'] : "Untitled",
            "description" => isset($row['description']) ? $row['description'] : "",
            "course_title" => isset($row['course_title']) ? $row['course_title'] : "General",
            "instructor_name" => isset($row['instructor_name']) ? $row['instructor_name'] : "Instructor",
            "due_date" => isset($row['due_date']) ? $row['due_date'] : null,
            "file_url" => isset($row['file_url']) ? $row['file_url'] : null
        );
    }
}

echo json_encode(["success" => true, "items" => $items]);
$conn->close();
?>

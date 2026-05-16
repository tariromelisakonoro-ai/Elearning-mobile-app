<?php
error_reporting(0);
header('Content-Type: application/json');
include "db.php";

$sql = "SELECT * FROM assignments";
$result = $conn->query($sql);

$assignments = array();
if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $assignments[] = array(
            "id" => isset($row['id']) ? $row['id'] : "",
            "title" => isset($row['title']) ? $row['title'] : "Untitled",
            "description" => isset($row['description']) ? $row['description'] : "",
            "course_title" => isset($row['course_title']) ? $row['course_title'] : "General",
            "instructor_name" => isset($row['instructor_name']) ? $row['instructor_name'] : "Instructor",
            "due_date" => isset($row['due_date']) ? $row['due_date'] : null
        );
    }
}

echo json_encode(["success" => true, "assignments" => $assignments]);
$conn->close();
?>

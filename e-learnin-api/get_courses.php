<?php
error_reporting(0);
header('Content-Type: application/json');
include "db.php";

$sql = "SELECT * FROM courses";
$result = $conn->query($sql);

$courses = array();
if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $courses[] = array(
            "title" => isset($row['title']) ? $row['title'] : "Untitled",
            "description" => isset($row['description']) ? $row['description'] : "",
            "instructor_name" => isset($row['instructor_name']) ? $row['instructor_name'] : (isset($row['fullname']) ? $row['fullname'] : "Unknown")
        );
    }
}

echo json_encode(["success" => true, "courses" => $courses]);
$conn->close();
?>

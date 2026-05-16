<?php
header("Content-Type: application/json");
include 'db_connect.php';

$sql = "SELECT c.id, c.name, u.name AS instructor
        FROM courses c
        LEFT JOIN users u ON c.instructor_id = u.id
        ORDER BY c.name ASC";

$result = $conn->query($sql);
$courses = [];

while ($row = $result->fetch_assoc()) {
    $courses[] = $row;
}

echo json_encode(["success" => true, "courses" => $courses]);
$conn->close();
?>
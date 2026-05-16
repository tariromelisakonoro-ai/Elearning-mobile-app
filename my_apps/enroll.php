<?php
header("Content-Type: application/json");
include 'db_connect.php';

$data      = json_decode(file_get_contents("php://input"), true);
$user_id   = intval($data['user_id']);
$course_id = intval($data['course_id']);

// Check if already enrolled
$check = $conn->query("SELECT id FROM enrollments WHERE user_id=$user_id AND course_id=$course_id");
if ($check->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Already enrolled in this course"]);
    exit;
}

$sql = "INSERT INTO enrollments (user_id, course_id) VALUES ($user_id, $course_id)";
if ($conn->query($sql)) {
    echo json_encode(["success" => true, "message" => "Enrolled successfully!"]);
} else {
    echo json_encode(["success" => false, "message" => "Enrollment failed"]);
}
$conn->close();
?>
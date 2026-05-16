<?php
header('Content-Type: application/json');
include "db.php";

$title = $_POST['title'];
$description = $_POST['description'];
$category = $_POST['category'];
$instructor_id = $_POST['user_id'];

// Get instructor name for easy display later
$userQuery = $conn->query("SELECT fullname FROM users WHERE id = '$instructor_id'");
$userData = $userQuery->fetch_assoc();
$instructor_name = $userData['fullname'];

$sql = "INSERT INTO courses (title, description, category, instructor_id, instructor_name)
        VALUES ('$title', '$description', '$category', '$instructor_id', '$instructor_name')";

if ($conn->query($sql)) {
    echo json_encode(["success" => true, "message" => "Course added successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
}
$conn->close();
?>

<?php
header("Content-Type: application/json");
include 'db_connect.php'; // Points to learnexpress_v2

$data = json_decode(file_get_contents("php://input"), true);

$fullname = $conn->real_escape_string($data['fullname']);
$email    = $conn->real_escape_string($data['email']);
$password = password_hash($data['password'], PASSWORD_DEFAULT);
$role     = isset($data['role']) ? $conn->real_escape_string($data['role']) : 'Student'; // Default role

// Check if email already exists
$check = $conn->query("SELECT id FROM users WHERE email = '$email'");
if ($check->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Email already registered"]);
    exit;
}

// Unified query using 'fullname' and 'role' to match mobile app
$sql = "INSERT INTO users (fullname, email, password, role) VALUES ('$fullname', '$email', '$password', '$role')";

if ($conn->query($sql)) {
    echo json_encode(["success" => true, "message" => "Registration successful"]);
} else {
    echo json_encode(["success" => false, "message" => "Registration failed: " . $conn->error]);
}
$conn->close();
?>

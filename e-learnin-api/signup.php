<?php
error_reporting(0);
header('Content-Type: application/json');
include "db.php";

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $fullname = isset($_POST['fullname']) ? $_POST['fullname'] : '';
    $email = isset($_POST['email']) ? $_POST['email'] : '';
    $password = isset($_POST['password']) ? $_POST['password'] : '';
    $role = isset($_POST['role']) ? $_POST['role'] : '';

    if (empty($fullname) || empty($email) || empty($password) || empty($role)) {
        echo json_encode(["status" => "error", "message" => "All fields are required"]);
        exit;
    }

    // Check if user already exists
    $checkStmt = $conn->prepare("SELECT email FROM users WHERE email = ?");
    $checkStmt->bind_param("s", $email);
    $checkStmt->execute();
    $checkResult = $checkStmt->get_result();

    if ($checkResult && $checkResult->num_rows > 0) {
        echo json_encode(["status" => "error", "message" => "Email already registered"]);
    } else {
        // ENCRYPT PASSWORD
        $hashed_password = password_hash($password, PASSWORD_DEFAULT);

        // Insert new user
        $stmt = $conn->prepare("INSERT INTO users (fullname, email, password, role) VALUES (?, ?, ?, ?)");
        if (!$stmt) {
            echo json_encode(["status" => "error", "message" => "Database error: " . $conn->error]);
            exit;
        }
        $stmt->bind_param("ssss", $fullname, $email, $hashed_password, $role);

        if ($stmt->execute()) {
            echo json_encode(["status" => "success", "message" => "User registered successfully"]);
        } else {
            echo json_encode(["status" => "error", "message" => "Registration failed: " . $stmt->error]);
        }
        $stmt->close();
    }
    $checkStmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
$conn->close();
?>

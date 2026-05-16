<?php
error_reporting(0);
header('Content-Type: application/json');
include "db.php";

$email = isset($_POST['email']) ? $_POST['email'] : '';
$password = isset($_POST['password']) ? $_POST['password'] : '';

if (empty($email) || empty($password)) {
    echo json_encode(["status" => "error", "message" => "Email and password are required"]);
    exit;
}

$stmt = $conn->prepare("SELECT fullname, role, password FROM users WHERE email = ?");
if (!$stmt) {
    echo json_encode(["status" => "error", "message" => "Database error: " . $conn->error]);
    exit;
}

$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result && $result->num_rows > 0) {
    $user = $result->fetch_assoc();
    // Use password_verify for encrypted passwords
    if (password_verify($password, $user['password'])) {
        echo json_encode([
            "status" => "success",
            "fullname" => $user['fullname'],
            "role" => $user['role']
        ]);
    } else {
        // Fallback for old plain text passwords during transition (optional, but safer to just enforce hash)
        if ($password === $user['password']) {
             echo json_encode([
                "status" => "success",
                "fullname" => $user['fullname'],
                "role" => $user['role']
            ]);
        } else {
            echo json_encode(["status" => "error", "message" => "Invalid password"]);
        }
    }
} else {
    echo json_encode(["status" => "error", "message" => "User not found"]);
}

$stmt->close();
$conn->close();
?>

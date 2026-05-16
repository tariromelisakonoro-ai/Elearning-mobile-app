<?php
header("Content-Type: application/json");
include 'db_connect.php';

$user_id = intval($_GET['user_id']);

$sql = "SELECT * FROM users WHERE id = $user_id";
$result = $conn->query($sql);

if ($result->num_rows === 1) {
    $user = $result->fetch_assoc();
    echo json_encode([
        "success" => true,
        "data" => [
            "class"            => $user['class'] ?? "—",
            "level"            => $user['level'] ?? "—",
            "status"           => $user['status'] ?? "Active",
            "gpa"              => $user['gpa'] ?? "—",
            "attendance"       => $user['attendance'] ?? "0",
            "balance"          => $user['balance'] ?? "0.00",
            "courses_enrolled" => $user['courses_enrolled'] ?? 0
        ]
    ]);
} else {
    echo json_encode(["success" => false, "message" => "User not found"]);
}
$conn->close();
?>
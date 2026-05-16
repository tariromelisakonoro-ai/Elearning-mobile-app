<?php
header('Content-Type: application/json');
include "db.php";

$sql = "SELECT * FROM announcements ORDER BY id DESC";
$result = $conn->query($sql);
$announcements = array();

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $announcements[] = array(
            "title" => $row['title'],
            "message" => $row['message'],
            "author_name" => isset($row['author_name']) ? $row['author_name'] : "LearnExpress Admin"
        );
    }
}

echo json_encode(["success" => true, "announcements" => $announcements]);
$conn->close();
?>

<?php
error_reporting(0);
header('Content-Type: application/json');
include "db.php";

// Fetching from the 'community_questions' table as per your XAMPP setup
$sql = "SELECT * FROM community_questions ORDER BY id DESC";
$result = $conn->query($sql);
$questions = array();

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $questions[] = array(
            "title" => isset($row['title']) ? $row['title'] : "Untitled",
            "subject" => isset($row['subject']) ? $row['subject'] : "General",
            "body" => isset($row['body']) ? $row['body'] : "",
            "upvotes" => isset($row['upvotes']) ? (int)$row['upvotes'] : 0,
            "author" => isset($row['author']) ? $row['author'] : "Anonymous",
            "created_at" => isset($row['created_at']) ? $row['created_at'] : "Recently"
        );
    }
}

echo json_encode(["success" => true, "questions" => $questions]);
$conn->close();
?>

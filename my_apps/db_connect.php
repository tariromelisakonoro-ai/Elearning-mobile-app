<?php
$host = "localhost";
$dbname = "learnexpress_v2"; // Unified to use the same database as the mobile app
$username = "root";             
$password = "";                
$conn = new mysqli($host, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Connection failed: " . $conn->connect_error]));
}
?>
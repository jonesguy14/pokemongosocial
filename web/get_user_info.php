<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    getUserInfo();
}

function getUserInfo() {
    global $pdo;

    $username = $_POST["username"];

    $stmt = $pdo->prepare("SELECT * FROM users WHERE username=? LIMIT 1");
    $stmt->execute([$username]);

    $result = $stmt->fetch();  

    if ($result) {
        $response = $result;
        $response["success"] = 1;
        $response["message"] = "Success!";
        echo json_encode($response);        
    } else {
        $response["success"] = 0;
        $response["message"] = "User not found!";
        echo json_encode($response);
    }
}
?>
<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    userLogin();
}

function userLogin() {
    global $pdo;

    $username = $_POST["username"];
    $password = $_POST["password"];

    // Prepare statements
    $stmt = $pdo->prepare("SELECT password FROM users WHERE username=? LIMIT 1");
    $stmt->execute([$username]);

    $result = $stmt->fetch();

    if ($result) {
        if (password_verify($password, $result["password"])) {
            $response["success"] = 1;
            $response["message"] = "Success!";
            echo json_encode($response);
        } else {
            $response["success"] = 0;
            $response["message"] = "Incorrect password!";
            echo json_encode($response);
        }
        
    } else {
        $response["success"] = 0;
        $response["message"] = "User not found!";
        echo json_encode($response);
    }
}
?>
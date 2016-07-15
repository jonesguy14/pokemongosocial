<?php

//header('Content-type : bitmap; charset=utf-8');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    newUser();
}
 

function newUser() {
    global $pdo;

    if (isset($_POST["username"]) && isset($_POST["password"]) && isset($_POST["team"]) && isset($_POST["profile_image_path"])) {

        $username = $_POST["username"];
        $password = $_POST["password"];
        $team = $_POST["team"];
        $profile_image_path = $_POST["profile_image_path"];

        $passwordHash = password_hash($password, PASSWORD_DEFAULT);
        
        try {
            $stmt = $pdo->prepare("INSERT INTO users values (?, ?, ?, ?, CURRENT_TIMESTAMP, 100)");
        
            if ($stmt->execute([$username, $passwordHash, $profile_image_path, $team])) {
                $response["success"] = 1;
                $response["message"] = "Successfully made new user.";
         
                // echoing JSON response
                echo json_encode($response);
            } else {
                $response["success"] = 0;
                $response["message"] = "Something went wrong.";
         
                // echoing JSON response
                echo json_encode($response);
            }
        } catch (PDOException $e) {
            $response["success"] = 0;
            $response["message"] = "Username already exists!";
     
            // echoing JSON response
            echo json_encode($response);
        }
        
        mysqli_close($connect);
    } else {
        $response["success"] = 0;
        $response["message"] = "Required field is missing.";
        echo json_encode($response);
    }
}
?>
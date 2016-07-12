<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    userLogin();
}

function userLogin() {
    global $connect;

    $username = $_POST["username"];
    $password = $_POST["password"];

    $query = "SELECT password FROM users WHERE username='$username' LIMIT 1;";

    $result = mysqli_query($connect, $query) or die(mysqli_error($connect));
    mysqli_close($connect);

    if (mysqli_num_rows($result) > 0) {
        $response = mysqli_fetch_assoc($result);
        if (password_verify($password, $response["password"])) {
            $response["success"] = 1;
            $response["message"] = "Success!";
            echo json_encode($response);
        } else {
            $response["success"] = 0;
            $response["message"] = "Incorrect password!";
            echo json_encode($response);
        }
        
    } else {
        $response = mysqli_fetch_assoc($result);
        $response["success"] = 0;
        $response["message"] = "User not found!";
        echo json_encode($response);
    }
}
?>
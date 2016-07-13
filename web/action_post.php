<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    checkLogin();
}

function actionPost() {
    global $connect;

    $post_id = $_POST["post_id"];
    $user_id = $_POST["username"];
    $content = $_POST["content"];

    $query = "INSERT INTO actions(user_id, post_id, content, time) VALUES ('$user_id', '$post_id', '$content', CURRENT_TIMESTAMP);";
    $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

    mysqli_close($connect);

    if ($result) {
        $response["success"] = 1;
        $response["message"] = "Success!";
        echo json_encode($response);
    } else {
        $response["success"] = 0;
        $response["message"] = "Error inserting new comment!";
        echo json_encode($response);
    }

}

function checkLogin() {
    global $connect;

    $username = $_POST["username"];
    $password = $_POST["password"];

    $query = "SELECT password FROM users WHERE username='$username' LIMIT 1;";

    $result = mysqli_query($connect, $query) or die(mysqli_error($connect));
    //mysqli_close($connect);

    if (mysqli_num_rows($result) > 0) {
        $response = mysqli_fetch_assoc($result);
        if (password_verify($password, $response["password"])) {
            //$response["success"] = 1;
            //$response["message"] = "Success!";
            actionPost();
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
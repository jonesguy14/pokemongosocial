<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    checkLogin();
}

function likePost() {
    global $connect;

    $post_id = $_POST["post_id"];
    $post_user_id = $_POST["post_user_id"];
    $isUpDown = $_POST["isUpDown"];

    if ($isUpDown === "UP") {
        // Give a +1 to the post
        $query = "UPDATE posts SET likes=likes+1 WHERE post_id='$post_id';";
        $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

        if ($result) {
            // Give a +1 to the user who made the post
            $query = "UPDATE users SET reputation=reputation+1 WHERE username='$post_user_id';";
            $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

            $response["success"] = 1;
            $response["message"] = "Success!";
            echo json_encode($response);
        } else {
            $response["success"] = 0;
            $response["message"] = "Error thumbing up!";
            echo json_encode($response);
        }

    }
    else if ($isUpDown === "DOWN") {
        // Give a -1 to the post
        $query = "UPDATE posts SET likes=likes-1 WHERE post_id='$post_id';";
        $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

        if ($result) {
            // Give a -1 to the user who made the post
            $query = "UPDATE users SET reputation=reputation-1 WHERE username='$post_user_id';";
            $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

            $response["success"] = 1;
            $response["message"] = "Success!";
            echo json_encode($response);
        } else {
            $response["success"] = 0;
            $response["message"] = "Error thumbing down!";
            echo json_encode($response);
        }
    }

    mysqli_close($connect);
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
            likePost();
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
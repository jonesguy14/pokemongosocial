<?php

//header('Content-type : bitmap; charset=utf-8');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    checkLogin();
}

function newPost() {
    global $connect;

    if( isset($_POST["title"]) && 
        isset($_POST["caption"]) &&
        isset($_POST["latitude"]) && 
        isset($_POST["longitude"]) &&
        isset($_POST["username"]) &&
        isset($_POST["team"]) ) {

        $username = $_POST["username"];
        $title = $_POST["title"];
        $caption = $_POST["caption"];
        $latitude = $_POST["latitude"];
        $longitude = $_POST["longitude"];
        $team = $_POST["team"];
            
        $query = "INSERT INTO posts(user_id, title, caption, time, latitude, longitude, user_team) values ('$username', '$title', '$caption', CURRENT_TIMESTAMP, '$latitude', '$longitude', '$team');";

        $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

        if($result){
            $response["success"] = 1;
            $response["message"] = "Successfully made new post.";
            $response["post_id"] = mysqli_insert_id($connect);
     
            // echoing JSON response
            echo json_encode($response);
        }else{
            $response["success"] = 0;
            $response["message"] = "Something went wrong.";
     
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
            newPost();
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
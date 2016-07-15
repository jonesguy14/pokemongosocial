<?php

//header('Content-type : bitmap; charset=utf-8');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';

    if (isset($_POST["username"]) && isset($_POST["password"])) {
        $loginCheck = checkLogin($_POST["username"], $_POST["password"]);
        if ($loginCheck["success"] === 1) {
            // Successful login
            newPost();
        } else {
            echo json_encode($loginCheck);
        }
    } else {
        $response["success"] = 0;
        $response["message"] = "Verification failed, username and password needed.";
        echo json_encode($response);
    }
    
}

function newPost() {
    global $pdo;

    if( isset($_POST["title"]) && 
        isset($_POST["caption"]) &&
        isset($_POST["latitude"]) && 
        isset($_POST["longitude"]) &&
        isset($_POST["username"]) &&
        isset($_POST["team"]) &&
        isset($_POST["only_visible_team"]) ) {

        $username = $_POST["username"];
        $title = $_POST["title"];
        $caption = $_POST["caption"];
        $latitude = $_POST["latitude"];
        $longitude = $_POST["longitude"];
        $team = $_POST["team"];
        $onlyVisibleTeam = $_POST["only_visible_team"];

        $stmt = $pdo->prepare("INSERT INTO posts(user_id, title, caption, time, latitude, longitude, user_team, only_visible_team) values (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)");

        // execute returns true on success
        if($stmt->execute([$username, $title, $caption, $latitude, $longitude, $team, $onlyVisibleTeam])) {
            $response["success"] = 1;
            $response["message"] = "Successfully made new post.";
            $response["post_id"] = $pdo->lastInsertId();
     
            // echoing JSON response
            echo json_encode($response);
        } else {
            $response["success"] = 0;
            $response["message"] = "Something went wrong.";
     
            // echoing JSON response
            echo json_encode($response);
        }

    } else {
        $response["success"] = 0;
        $response["message"] = "Required field is missing.";
        echo json_encode($response);
    }
}



?>
<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    
    if (isset($_POST["username"]) && isset($_POST["password"])) {
        $loginCheck = checkLogin($_POST["username"], $_POST["password"]);
        if ($loginCheck["success"] === 1) {
            // Successful login
            likePost();
        } else {
            echo json_encode($loginCheck);
        }
    } else {
        $response["success"] = 0;
        $response["message"] = "Verification failed, username and password needed.";
        echo json_encode($response);
    }
}

function likePost() {
    global $pdo;

    if (isset($_POST["post_id"]) && isset($_POST["post_user_id"]) && isset($_POST["isUpDown"])) {

        $post_id = $_POST["post_id"];
        $post_user_id = $_POST["post_user_id"];
        $isUpDown = $_POST["isUpDown"];

        if ($isUpDown === "UP") {
            $change = 1;
        } else {
            $change = -1;
        }

        $stmt = $pdo->prepare("UPDATE posts SET likes=likes+(?) WHERE post_id=?");

        // execute returns true on success
        if ($stmt->execute([$change, $post_id])) {

            // Give rep to the user who made the post
            $stmt = $pdo->prepare("UPDATE users SET reputation=reputation+(?) WHERE username=?");

            // execute returns true on success
            if ($stmt->execute([$change, $post_user_id])) {
                $response["success"] = 1;
                $response["message"] = "Success!";
                echo json_encode($response);
            } else {
                $response["success"] = 0;
                $response["message"] = "Error changing reputation!";
                echo json_encode($response);
            }
            
        } else {
            $response["success"] = 0;
            $response["message"] = "Error thumbing up/down!";
            echo json_encode($response);
        }

    } else {
        $response["success"] = 0;
        $response["message"] = "Required field is missing.";
        echo json_encode($response);
    }

}

?>
<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    
    if (isset($_POST["username"]) && isset($_POST["password"])) {
        $loginCheck = checkLogin($_POST["username"], $_POST["password"]);
        if ($loginCheck["success"] === 1) {
            // Successful login
            actionPost();
        } else {
            echo json_encode($loginCheck);
        }
    } else {
        $response["success"] = 0;
        $response["message"] = "Verification failed, username and password needed.";
        echo json_encode($response);
    }
}

function actionPost() {
    global $pdo;

    if( isset($_POST["post_id"]) && 
        isset($_POST["content"]) ) {

        $post_id = $_POST["post_id"];
        $user_id = $_POST["username"];
        $content = $_POST["content"];

        $length = mb_strlen($content, 'utf8');
        if ($length === FALSE || $length > 140) {
            return;
        }

        $stmt = $pdo->prepare("INSERT INTO actions(user_id, post_id, content, time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)");

        // execute returns true on success
        if ($stmt->execute([$user_id, $post_id, $content])) {
            $response["success"] = 1;
            $response["message"] = "Success!";
            echo json_encode($response);
        } else {
            $response["success"] = 0;
            $response["message"] = "Error inserting new comment!";
            echo json_encode($response);
        }

    } else {
        $response["success"] = 0;
        $response["message"] = "Required field is missing.";
        echo json_encode($response);
    }

}

?>

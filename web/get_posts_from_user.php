<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    getPostsFromUser();
}

function getPostsFromUser() {
    global $pdo;

    if (isset($_POST["username"])) {
        $username = $_POST["username"];

        $stmt = $pdo->prepare(
            "SELECT post_id, user_id, title, caption, TIMESTAMPDIFF(minute, time, CURRENT_TIMESTAMP) as time, latitude, longitude, likes, user_team, only_visible_team
             from posts where user_id=? ORDER BY post_id DESC");
        $stmt->execute([$username]);

        $result = $stmt->fetchAll();  

        if ($result) {
            // echoing JSON response
            $response = $result;
            $response["num_rows"] = $stmt->rowCount();
            $response["success"] = 1;
            $response["message"] = "Got all posts as a JSON.";
            echo json_encode($response);
        } else {
            // Didn't find it
            $response["success"] = 0;
            $response["message"] = "No posts found.";
     
            // echoing JSON response
            echo json_encode($response);
        }

    } else {
        // required field is missing
        $response["success"] = 0;
        $response["message"] = "Required field is missing.";
     
        // echoing JSON response
        echo json_encode($response);
    }   
    
}

?>
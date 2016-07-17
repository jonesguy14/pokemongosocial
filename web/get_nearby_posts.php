<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    getNearbyPosts();
}

function getNearbyPosts() {
    global $pdo;

    if (isset($_POST["latitude"]) && isset($_POST["longitude"]) && isset($_POST["range"]) && isset($_POST["team"])) {
        
        $latitude = $_POST["latitude"];
        $longitude = $_POST["longitude"];
        $range = $_POST["range"];
        $team = $_POST["team"];

        $latTopRange = $latitude + $range;
        $latBotRange = $latitude - $range;
        $lonTopRange = $longitude + $range;
        $lonBotRange = $longitude - $range;

        $stmt = $pdo->prepare("SELECT post_id, user_id, title, caption, TIMESTAMPDIFF(minute, time, CURRENT_TIMESTAMP) as time, latitude, longitude, likes, user_team, only_visible_team
            FROM posts WHERE 
            latitude < ? AND latitude > ? AND 
            longitude < ? AND longitude > ? AND 
            (only_visible_team = 0 OR user_team = ?) AND
            TIMESTAMPDIFF(hour, time, CURRENT_TIMESTAMP) <= 24");
        $stmt->execute([$latTopRange, $latBotRange, $lonTopRange, $lonBotRange, $team]);

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
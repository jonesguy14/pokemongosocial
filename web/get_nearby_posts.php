<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    getNearbyPosts();
}

function getNearbyPosts() {
    global $connect;

    if (isset($_POST["latitude"]) && isset($_POST["longitude"])) {
        
        $latitude = $_POST["latitude"];
        $longitude = $_POST["longitude"];
        $range = $_POST["range"];
        $team = $_POST["team"];

        $query = "SELECT post_id, user_id, title, caption, TIMESTAMPDIFF(minute, time, CURRENT_TIMESTAMP) as time, latitude, longitude, likes, user_team, only_visible_team
            FROM posts WHERE 
            latitude < ($latitude + $range) AND 
            latitude > ($latitude - $range) AND 
            longitude < ($longitude + $range) AND 
            longitude > ($longitude - $range) AND 
            (only_visible_team = 0 OR user_team = '$team');";

        $result = mysqli_query($connect, $query) or die(mysqli_error($connect));
        mysqli_close($connect);

        $numRows = mysqli_num_rows($result);
        if ($numRows > 0) {
            // echoing JSON response
            $response = array();
            while($r = mysqli_fetch_assoc($result)) {
                $response[] = $r;
            }
            $response["num_rows"] = $numRows;
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
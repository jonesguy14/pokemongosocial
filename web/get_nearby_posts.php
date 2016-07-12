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

        $range = 100;

        $query = "SELECT * from posts where latitude < ($latitude + $range) AND latitude > ($latitude - $range) AND longitude < ($longitude + $range) AND longitude > ($longitude - $range);";

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
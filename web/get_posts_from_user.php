<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    getPostsFromUser();
}

function getPostsFromUser() {
    global $pdo;

    if (isset($_POST["username"])) {
        $username = $_POST["username"];

        $stmt = $pdo->prepare("SELECT * from posts where user_id=?");
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
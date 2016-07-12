<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    getNumLikes();
}

function getNumLikes() {
    global $connect;

    $post_id = $_POST["post_id"];
    $username = $_POST["username"];

    //$query = "SELECT action_id as num_rows from actions where post_id='$post_id' and action_type='THUMB';";
    $query = "SELECT likes from posts where post_id='$post_id';";
    $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

    if ($result) {
        $response = mysqli_fetch_assoc($result);
        $query = "SELECT action_id from actions where post_id='$post_id' and action_type='THUMB' and user_id='$username';";
        $result = mysqli_query($connect, $query) or die(mysqli_error($connect));
        $response["has_liked"] = mysqli_num_rows($result);
        $response["num_rows"] = $numRows;
        $response["success"] = 1;
        $response["message"] = "Success!";
        echo json_encode($response);
    } else {
        $response["num_rows"] = 0;
        $response["success"] = 0;
        $response["message"] = "No likes!";
        echo json_encode($response);
    }

}
?>
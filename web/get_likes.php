<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    getAllUserLikes();
}

function getAllUserLikes() {
    global $connect;

    $post_id = $_POST["post_id"];

    $query = "SELECT * from users where username IN (SELECT user_id from actions where post_id='$post_id' and action_type='THUMB');";
    $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

    $numRows = mysqli_num_rows($result);
    if ($numRows > 0) {
        $response = array();
        while($r = mysqli_fetch_assoc($result)) {
            $response[] = $r;
        }
        $response["num_rows"] = $numRows;
        $response["success"] = 1;
        $response["message"] = "Success!";
        echo json_encode($response);
    } else {
        $response["success"] = 0;
        $response["message"] = "Post not found or no likes!";
        echo json_encode($response);
    }

}
?>
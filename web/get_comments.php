<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    getAllUserComments();
}

function getAllUserComments() {
    global $connect;

    $post_id = $_POST["post_id"];

    $query = "SELECT a.content, a.time, u.username, u.team, u.profile_image_path FROM 
                (SELECT * FROM actions WHERE post_id='$post_id' and action_type='COMMENT') as a 
                INNER JOIN users as u ON a.user_id=u.username;";
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
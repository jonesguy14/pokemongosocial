<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    getAllUserComments();
}

function getAllUserComments() {
    global $pdo;

    $post_id = $_POST["post_id"];

    $stmt = $pdo->prepare("SELECT a.action_id, a.content, TIMESTAMPDIFF(minute, a.time, CURRENT_TIMESTAMP) as time, a.likes, u.username, u.team, u.profile_image_path FROM 
                (SELECT * FROM actions WHERE post_id=?) as a 
                INNER JOIN users as u ON a.user_id=u.username
                ORDER BY a.likes DESC");
    $stmt->execute([$post_id]);

    $result = $stmt->fetchAll();  

    if ($result) {
        $response = $result;
        $response["num_rows"] = $stmt->rowCount();
        $response["success"] = 1;
        $response["message"] = "Success!";
        echo json_encode($response);
    } else {
        $response["success"] = 0;
        $response["message"] = "No comments found!";
        echo json_encode($response);
    }

}
?>
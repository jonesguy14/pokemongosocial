<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    getNumLikes();
}

function getNumLikes() {
    global $pdo;

    $post_id = $_POST["post_id"];

    $stmt = $pdo->prepare("SELECT likes from posts where post_id=?");
    $stmt->execute([$post_id]);

    $result = $stmt->fetch();

    if ($result) {
        $response = $result;
        $response["success"] = 1;
        $response["message"] = "Success!";
        echo json_encode($response);
    } else {
        $response["success"] = 0;
        $response["message"] = "Post not found!";
        echo json_encode($response);
    }

}
?>
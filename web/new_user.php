<?php

//header('Content-type : bitmap; charset=utf-8');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    newUser();
}
 

function newUser() {
    global $connect;

    $response["input"] = file_get_contents('php://input');

    if(isset($_POST["username"])){

        $username = $_POST["username"];
        $password = $_POST["password"];
        $team = $_POST["team"];
        $profile_image_path = $_POST["profile_image_path"];

        $passwordHash = password_hash($password, PASSWORD_DEFAULT);
        
        $query = "INSERT INTO users values ('$username', '$passwordHash', '$profile_image_path', '$team', CURRENT_TIMESTAMP);";
        
        $result = mysqli_query($connect, $query) or die(mysqli_error($connect));
        
        if($result){
            $response["success"] = 1;
            $response["message"] = "Successfully made new user.";
     
            // echoing JSON response
            echo json_encode($response);
        } else {
            $response["success"] = 0;
            $response["message"] = "Something went wrong.";
     
            // echoing JSON response
            echo json_encode($response);
        }
        
        mysqli_close($connect);
    } else {
        $response["success"] = 0;
        $response["message"] = "Required field is missing.";
        echo json_encode($response);
    }
}
?>
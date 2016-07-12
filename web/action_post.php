<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    checkLogin();
}

function actionPost() {
    global $connect;

    $post_id = $_POST["post_id"];
    $user_id = $_POST["username"];
    $action_type = $_POST["action_type"];
    $content = $_POST["content"];

    if ($action_type === "THUMB") {
        // Check if this user has already liked this post
        $query = "SELECT user_id from actions where post_id='$post_id' and user_id='$user_id' and action_type='THUMB';";
        $result = mysqli_query($connect, $query) or die(mysqli_error($connect));
        if (mysqli_num_rows($result) >= 1) {
            // Already liked!
            $response["result"] = $result;
            $response["success"] = 0;
            $response["message"] = "Already liked this post!";
            echo json_encode($response);
        } else {
            // First add like to post
            if ($content === "THUMB UP") {
                $query = "UPDATE posts SET likes=likes+1 WHERE post_id='$post_id';";
                $result = mysqli_query($connect, $query) or die(mysqli_error($connect));
            
                if ($result) {
                    // Next add reputation
                    $query = "UPDATE users SET reputation=reputation+1 WHERE username=(SELECT user_id FROM posts WHERE post_id='$post_id');";
                    $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

                    if ($result) {
                        // Next create action
                        $query = "INSERT INTO actions(user_id, post_id, action_type, content, time) VALUES ('$user_id', '$post_id', 'THUMB', '$content', CURRENT_TIMESTAMP);";
                        $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

                        mysqli_close($connect);

                        if ($result) {
                            $response["success"] = 1;
                            $response["message"] = "Success!";
                            echo json_encode($response);
                        } else {
                            $response["success"] = 0;
                            $response["message"] = "Error inserting new action!";
                            echo json_encode($response);
                        }
                    } else {
                        $response["success"] = 0;
                        $response["message"] = "Couldn't update reputation!";
                        echo json_encode($response);
                    }
                } else {
                    $response["success"] = 0;
                    $response["message"] = "Post not found!";
                    echo json_encode($response);
                }
            } 
            else if ($content === "THUMB DOWN") {
                $query = "UPDATE posts SET likes=likes-1 WHERE post_id='$post_id';";
                $result = mysqli_query($connect, $query) or die(mysqli_error($connect));
            
                if ($result) {
                    // Next add reputation
                    $query = "UPDATE users SET reputation=reputation-1 WHERE username=(SELECT user_id FROM posts WHERE post_id='$post_id');";
                    $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

                    if ($result) {
                        // Next create action
                        $query = "INSERT INTO actions(user_id, post_id, action_type, content, time) VALUES ('$user_id', '$post_id', 'THUMB', '$content', CURRENT_TIMESTAMP);";
                        $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

                        mysqli_close($connect);

                        if ($result) {
                            $response["success"] = 1;
                            $response["message"] = "Success!";
                            echo json_encode($response);
                        } else {
                            $response["success"] = 0;
                            $response["message"] = "Error inserting new action!";
                            echo json_encode($response);
                        }
                    } else {
                        $response["success"] = 0;
                        $response["message"] = "Couldn't update reputation!";
                        echo json_encode($response);
                    }
                } else {
                    $response["success"] = 0;
                    $response["message"] = "Post not found!";
                    echo json_encode($response);
                }
            } else {
                $response["success"] = 0;
                $response["message"] = "Invalid action type!";
                echo json_encode($response);
            }
        }
    } 
    else if ($action_type === "COMMENT") 
    {
        $query = "INSERT INTO actions(user_id, post_id, action_type, content, time) VALUES ('$user_id', '$post_id', 'COMMENT', '$content', CURRENT_TIMESTAMP);";
        $result = mysqli_query($connect, $query) or die(mysqli_error($connect));

        mysqli_close($connect);

        if ($result) {
            $response["success"] = 1;
            $response["message"] = "Success!";
            echo json_encode($response);
        } else {
            $response["success"] = 0;
            $response["message"] = "Error inserting new action!";
            echo json_encode($response);
        }
    } else {
        $response["success"] = 0;
        $response["message"] = "Incorrect action_type!";
        echo json_encode($response);
    }
}

function checkLogin() {
    global $connect;

    $username = $_POST["username"];
    $password = $_POST["password"];

    $query = "SELECT password FROM users WHERE username='$username' LIMIT 1;";

    $result = mysqli_query($connect, $query) or die(mysqli_error($connect));
    //mysqli_close($connect);

    if (mysqli_num_rows($result) > 0) {
        $response = mysqli_fetch_assoc($result);
        if (password_verify($password, $response["password"])) {
            //$response["success"] = 1;
            //$response["message"] = "Success!";
            actionPost();
        } else {
            $response["success"] = 0;
            $response["message"] = "Incorrect password!";
            echo json_encode($response);
        }
        
    } else {
        $response = mysqli_fetch_assoc($result);
        $response["success"] = 0;
        $response["message"] = "User not found!";
        echo json_encode($response);
    }
}

?>
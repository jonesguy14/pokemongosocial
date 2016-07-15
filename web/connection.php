<?php

$host = 'localhost';
$dbName = 'wandjpow_pokemon';
$user = 'wandjpow_admin';
$pass = 'wandr-Wasd1234!';
$charset = 'utf8';

$dsn = "mysql:host=$host;port=3306;dbname=$dbName;charset=$charset";
$opt = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $opt);
} catch(PDOException $e) {
    die('Could not connect to the database:<br/>' . $e);
}

function checkLogin($username, $password) {
    global $pdo;
    
    $stmt = $pdo->prepare("SELECT password FROM users WHERE username=? LIMIT 1");
    $stmt->execute([$username]);

    $result = $stmt->fetch();

    if ($result) {
        if (password_verify($password, $result["password"])) {
            $response["success"] = 1;
            $response["message"] = "Success!";
            return $response;
        } else {
            $response["success"] = 0;
            $response["message"] = "Incorrect password!";
            return $response;
        }
        
    } else {
        $response = $result;
        $response["success"] = 0;
        $response["message"] = "User not found!";
        return $response;
    }
}

?>
<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require 'connection.php';
    createStudent();
}

function createStudent() {
    global $connect;

    $name = $_POST["name"];
    $age = $_POST["age"];

    $query = " Insert into students(name,age) values ('$name','$age');";

    mysqli_query($connect, $query) or die(mysqli_error($connect));
    mysqli_close($connect);
}
?>
<?php

define('hostname', 'localhost');
define('user', 'wandjpow_admin');
define('password', 'wandr-Wasd1234!');
define('databaseName', 'wandjpow_pokemon');

$connect = mysqli_connect(hostname, user, password, databaseName, 3306);
if (mysqli_connect_errno())
{
    echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

?>
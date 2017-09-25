<?php
/*
* Update the location of the device to the server
*/
// array for JSON response
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$user_name = $_POST["user_name"];
$lat = $_POST["lat"];
$lon = $_POST["lon"];
$result = mysqli_query($db, "UPDATE users SET lat = '$lat', lon = '$lon' WHERE user_name = '$user_name'");

if ($result) {
    // successfully updated
    $response["success"] = 1;
    $response["message"] = "Location updated";
} else {
    // no journey found
    $response["success"] = 0;
    $response["message"] = "Oops! An error occurred.";
}
 
// echoing JSON response
echo json_encode($response);
mysqli_close($db);
?>
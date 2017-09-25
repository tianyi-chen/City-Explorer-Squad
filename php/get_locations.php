<?php
/*
 * Get the locations of all members of a journey
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);
$response["locations"]=array();

$journey_id = $_GET["journey_id"];
$user_names = mysqli_query($db, "SELECT user_name FROM user_journey_pairs WHERE journey_id = $journey_id");

while($row=mysqli_fetch_array($user_names)) {
    $user_name = $row["user_name"];
    $result = mysqli_query($db, "SELECT lat, lon FROM users WHERE user_name = '$user_name'");
    $row=mysqli_fetch_array($result);

    $location = array();
    $location["lat"]=$row["lat"];
    $location["lon"]=$row["lon"];
    
    array_push($response["locations"], $location);
}

$response["success"] = 1;
echo json_encode($response);
mysqli_close($db);
?>
<?php
/*
 * Add a blank journey
 */ 
// array for JSON response
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$user_name = $_POST["user_name"];
 
// mysql inserting a new row
$result = mysqli_query($db, "INSERT INTO journeys (city, date, members, achievements, points) VALUES ('NEW_JOURNEY', '', 1, 0, 0)");

if ($result) {
    // successfully inserted into database
    $result = mysqli_query($db, "SELECT journey_id FROM journeys WHERE city = 'NEW_JOURNEY'");
    $row = mysqli_fetch_array($result);
    $journey_id = $row["journey_id"];
    $response["journey_id"] = $journey_id;
    $result = mysqli_query($db, "INSERT INTO user_journey_pairs (user_name, journey_id) VALUES ('$user_name', $journey_id)");
    if ($result) {
    	$response["success"] = 1;
    	$response["message"] = "Journey successfully created";
    } else {
    	$response["success"] = 0;
    	$response["message"] = "Failed to add ". $user_name ." to journey";
    }
    
} else {
    // failed to insert row
    $response["success"] = 0;
    $response["message"] = "Failed to create journey";
}

// echoing JSON response
echo json_encode($response);
mysqli_close($db);
?>
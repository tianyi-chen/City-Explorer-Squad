<?php
/*
 * Update a journey with city and date
 */
// array for JSON response
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

//required field
$journey_id = $_POST["journey_id"];
$city = $_POST["city"];
$date = $_POST["date"];
// mysql update row with matched id
$result = mysqli_query($db, "UPDATE journeys SET city = '$city', date = '$date' WHERE journey_id = $journey_id");
 
if ($result) {
    // successfully updated
    $response["success"] = 1;
    $response["message"] = "Journey updated";
} else {
    // no journey found
    $response["success"] = 0;
    $response["message"] = "Oops! An error occurred.";
}
 
// echoing JSON response
echo json_encode($response);
mysqli_close($db);
?>
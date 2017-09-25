<?php
/*
* Update the status of a task
*/
// array for JSON response
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$journey_id = $_POST["journey_id"];
$task_id = $_POST["task_id"];
$added_points = $_POST["points"];

$result = mysqli_query($db, "UPDATE journey_task_pairs SET status = 'completed' WHERE journey_id = $journey_id && task_id = $task_id");

if ($result) {

	$result1 = mysqli_query($db, "UPDATE journeys SET achievements = achievements + 1 WHERE journey_id = $journey_id");
	$result2 = mysqli_query($db, "UPDATE journeys SET points = points + $added_points WHERE journey_id = $journey_id");
	if ($result1 && $result2) {
		 // successfully updated
    	$response["success"] = 1;
    	$response["message"] = "You have completed this task!";
	} else {
		// unable to update row
    	$response["success"] = 0;
    	$response["message"] = "Oops! An error occurred.";
	}
   
} else {
    // no journey found
    $response["success"] = 0;
    $response["message"] = "Oops! An error occurred.";
}
 
// echoing JSON response
echo json_encode($response);
mysqli_close($db);
?>
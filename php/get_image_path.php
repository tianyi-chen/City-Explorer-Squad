<?php
/*
 * Get image path
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$journey_id = $_GET['journey_id'];
$task_id = $_GET['task_id'];
$result = mysqli_query($db, "SELECT image_path FROM journey_task_pairs WHERE journey_id = $journey_id AND task_id = $task_id");
 
    if (mysqli_num_rows($result) > 0) {
 
        $row = mysqli_fetch_array($result); 
        $image_path = array();

        if ($row["image_path"] != null) {
            $image_path["image_path"] = $row["image_path"];
        // success
        $response["success"] = 1; 
        $response["image"] = array();
        array_push($response["image"], $image_path);
        } else {
            $response["success"] = 0;
            $response["message"] = "No image found";
        }   
            
    } else {
        // no task found
        $response["success"] = 0;
        $response["message"] = "No task found";
    }

// echoing JSON response
echo json_encode($response);
mysqli_close($db);
?>
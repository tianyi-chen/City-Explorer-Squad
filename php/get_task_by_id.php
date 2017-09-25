<?php
/*
 * Get a task by _id
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$journey_id = $_GET['journey_id'];
$task_id = $_GET['task_id'];
$result = mysqli_query($db, "SELECT * FROM tasks WHERE task_id = $task_id");
 
if (!empty($result)) {
    // check for empty result
    if (mysqli_num_rows($result) > 0) {
 
        $row = mysqli_fetch_array($result);
 
        $task= array();
        $task["task_name"] = $row["task_name"];
        $task["city"] = $row["city"];
        $task["content"] = $row["content"];
        $task["points"] = $row["points"];

        $result = mysqli_query($db, "SELECT status FROM journey_task_pairs WHERE task_id = $task_id && journey_id = $journey_id");
        if (mysqli_num_rows($result) > 0) {
            $row = mysqli_fetch_array($result);
            $task["status"] = $row["status"];
        }

        // success
        $response["success"] = 1;
        // user node
        $response["task"] = array();
        array_push($response["task"], $task);
        
            
        } else {
            // no task found
        $response["success"] = 0;
        $response["message"] = "No task found";
 
            
        }
    } else {
        // task found
        $response["success"] = 0;
        $response["message"] = "No task found";
 
       
    }

// echoing JSON response
echo json_encode($response);
mysqli_close($db);
?>
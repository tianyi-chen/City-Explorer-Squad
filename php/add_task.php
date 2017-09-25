<?php
/*
 * Add a task by task_id
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$task_id = $_POST["task_id"];
$journey_id = $_POST["journey_id"];

$result = mysqli_query($db, "SELECT * FROM journey_task_pairs WHERE task_id = $task_id && journey_id = $journey_id");
if (mysqli_num_rows($result) > 0) {
    // task already exists
    $response["success"] = 0;
    $response["message"] = "Task already exists";
} else {

    $result = mysqli_query($db, "SELECT * FROM tasks WHERE task_id = $task_id");
 
    $user = mysqli_fetch_array($result);

    $result = mysqli_query($db, "INSERT INTO journey_task_pairs (journey_id, task_id, status) VALUES ($journey_id, $task_id, 'uncompleted')");

    if ($result) {
        
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Task successfully added";
        
    } else {
        // failed to insert row
        $response["success"] = 0;
        $response["message"] = "Oops! An error occurred.";
    }

}

// echoing JSON response
echo json_encode($response);
mysqli_close($db);
?>
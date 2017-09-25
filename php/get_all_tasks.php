<?php
/*
 * Get all tasks of a city
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$city = $_GET["city"];
$result = mysqli_query($db, "SELECT * FROM tasks WHERE city = '$city'");
$response["tasks"]=array();
 
if (mysqli_num_rows($result) > 0) {

    while($row=mysqli_fetch_array($result)) {
        $task=array();
        $task["task_id"]=$row["task_id"];
        $task["task_name"]=$row["task_name"];
        $task["points"]=$row["points"];

        array_push($response["tasks"], $task);
    }

    $response["success"] = 1;
 
} else {
    // no task found
    $response["success"] = 0;
    $response["message"] = "No task found for this city";

}
// echo JSON
echo json_encode($response);
mysqli_close($db);
?>
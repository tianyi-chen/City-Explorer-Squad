<?php
/*
 * Get the tasks of a journey
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$journey_id = $_GET["journey_id"];
$result = mysqli_query($db, "SELECT * FROM journey_task_pairs WHERE journey_id = $journey_id");
$response["tasks"]=array();
 
if (mysqli_num_rows($result) > 0) {

    while($row=mysqli_fetch_array($result)) {
        $task=array();
        $task["task_id"]=$row["task_id"];
        $task["status"]=$row["status"];

        $result1 = mysqli_query($db, "SELECT task_name,points FROM tasks WHERE task_id = $task[task_id]");

        $row1=mysqli_fetch_array($result1);
        $task["task_name"]=$row1["task_name"];
        $task["points"]=$row1["points"];
        array_push($response["tasks"], $task);
    }

    $response["success"] = 1;
 
} else {
    // no task found
    $response["success"] = 0;
    $response["message"] = "No task found";

}
// echo no users JSON
echo json_encode($response);
mysqli_close($db);
?>
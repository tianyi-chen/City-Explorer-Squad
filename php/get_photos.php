<?php
/*
 * Get the photos of a journey
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$journey_id = $_GET["journey_id"];
$result = mysqli_query($db, "SELECT * FROM journey_task_pairs WHERE journey_id = $journey_id AND image_path <> ''");
$response["paths"]=array();
 
if (mysqli_num_rows($result) > 0) {

    while($row=mysqli_fetch_array($result)) {
        $photo=array();
        $photo["image_path"]=$row["image_path"];
        array_push($response["paths"], $photo);
    }

    $response["success"] = 1;
 
} else {
    // no task found
    $response["success"] = 0;
    $response["message"] = "No photo found";

}
// echo no users JSON
echo json_encode($response);
mysqli_close($db);
?>
<?php
/*
* Upload photo to server and save the image path in database
*/
// $response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$file_path = "uploads/";
$journey_id = $_POST["journey_id"];
$task_id = $_POST["task_id"];
     
$file_path = $file_path . basename($_FILES['uploaded_file']['name']);
if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {

	$result = mysqli_query($db, "UPDATE journey_task_pairs SET image_name = '".$_FILES['uploaded_file']['tmp_name']."', image_path = '".$file_path."' WHERE journey_id = $journey_id && task_id = $task_id");
	if ($result) {
		$response["success"] = 1;
		$response["message"] = "Photo successfully uploaded";

	} else {
		$response["success"] = 0;
		$response["message"] = "Failed to update image path";
	}
} else{
	$response["success"] = 0;
	$response["message"] = "Failed to upload photo";

}
// echo no users JSON
echo json_encode($response);
mysqli_close($db);
?>
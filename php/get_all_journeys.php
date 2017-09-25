<?php
/*
* Get all journeys of a particular user
*/
// array for JSON response
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$user_name = $_GET["user_name"];
// mysql select journey_ids with matched member_name
$ids = mysqli_query($db, "SELECT journey_id FROM user_journey_pairs WHERE user_name = '$user_name'");
if(mysqli_num_rows($ids) > 0) {
	// journeys found
	$response["journeys"]=array();

	while($result = mysqli_fetch_array($ids)) {
		$journey_id = $result['journey_id'];
		// mysql select rows with matched _id
		$row = mysqli_query($db, "SELECT * FROM journeys WHERE journey_id = $journey_id");
		$result = mysqli_fetch_array($row);

		$journey=array();
		$journey["journey_id"]=$result["journey_id"];
		$journey["city"]=$result["city"];
		$journey["date"]=$result["date"];
		array_push($response["journeys"], $journey);
	}
	
	$response["success"]=1;

} else {
	// this user has nor journey record
	$response["success"]=0;
	$response["message"]="No journey found";

}
echo json_encode($response);
mysqli_close($db);
?>
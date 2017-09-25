<?php
/*
* Post its ip address to the server
*/
// array for JSON response
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$user_name = $_POST["user_name"];
$result = mysqli_query($db, "SELECT * FROM users WHERE user_name = '$user_name'");
$debug = empty($result);
if (empty(mysqli_fetch_array($result))) {
	// create new user
	$result=mysqli_query($db, "INSERT INTO users (user_name, ip_address) VALUES ('$user_name','$_SERVER[REMOTE_ADDR]')");
	if ($result) {
		$response["success"]=1;
		$response["message"]="New user ". $user_name ." created";
	} else {
		$response["success"]=0;
		$response["message"]="Failed to create new user";
	}
} else {
	// user name already exists
	// login as the existing user and update the ip address
	mysqli_query($db, "UPDATE users SET ip_address = '$_SERVER[REMOTE_ADDR]' WHERE user_name = '$user_name'");
	$response["success"]=1;
	$response["message"]="Login as ".$user_name;
}

echo json_encode($response);
mysqli_close($db);
?>
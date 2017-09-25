<?php
/*
 * Get all gifts in the shop
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$result = mysqli_query($db, "SELECT * FROM gifts");
$response["gifts"]=array();

if (mysqli_num_rows($result) > 0) {

    while($row=mysqli_fetch_array($result)) {
        
        $gift=array();
        $gift["gift_id"]=$row["gift_id"];
        $gift["gift_name"]=$row["gift_name"];
        $gift["points"]=$row["points"];
        array_push($response["gifts"], $gift);
    }

    $response["success"] = 1;
 
} else {
    // no gift found
    $response["success"] = 0;
    $response["message"] = "No gift found in the system";

}
// echo JSON
echo json_encode($response);
mysqli_close($db);
?>
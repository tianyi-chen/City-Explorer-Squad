<?php
/*
 * Get a journey by _id
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

if (isset($_GET["journey_id"])) {
    $journey_id = $_GET['journey_id'];
    $result = mysqli_query($db, "SELECT * FROM journeys WHERE journey_id = $journey_id");
 
    if (!empty($result)) {
        // check for empty result
        if (mysqli_num_rows($result) > 0) {
 
            $result = mysqli_fetch_array($result);
 
            $journey = array();
            $journey["journey_id"] = $result["journey_id"];
            $journey["city"] = $result["city"];
            $journey["date"] = $result["date"];
            $journey["members"] = $result["members"];
            $journey["achievements"] = $result["achievements"];
            $journey["points"] = $result["points"];

            // success
            $response["success"] = 1;
            // user node
            $response["journey"] = array();
            array_push($response["journey"], $journey);
 
        } else {
            // no journey found
            $response["success"] = 0;
            $response["message"] = "No journey found";
 
        }
    } else {
        // no journey found
        $response["success"] = 0;
        $response["message"] = "No journey found";
 
    }
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
 
}
// echoing JSON response
echo json_encode($response);
mysqli_close($db);
?>
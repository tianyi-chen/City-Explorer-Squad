<?php
/*
 * Get the members of a journey
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

if (isset($_GET["journey_id"])) {
    $journey_id = $_GET['journey_id'];
    $result = mysqli_query($db, "SELECT * FROM user_journey_pairs WHERE journey_id = $journey_id");
    $response["members"]=array();
 
    if (mysqli_num_rows($result) > 0) {

        while($row=mysqli_fetch_array($result)) {
            $member=array();
            $member["user_name"]=$row["user_name"];
            $member["journey_id"]=$row["journey_id"];
            array_push($response["members"], $member);
        }

        $response["success"] = 1;

    } else {
        // no member found
        $response["success"] = 0;
        $response["message"] = "No member found";
 
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
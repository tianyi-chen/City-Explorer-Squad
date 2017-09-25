<?php
/*
 * Add a member by member_name
 */
$response = array();
// include db config class
require_once __DIR__ . '/db_config.php';
// connecting to db
$db = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);

$user_name = $_POST["user_name"];
$journey_id = $_POST["journey_id"];
$result = mysqli_query($db, "SELECT * FROM users WHERE user_name = '$user_name'");
 
if (mysqli_num_rows($result) > 0) {
 
    $user = mysqli_fetch_array($result);

    $result = mysqli_query($db, "SELECT * FROM user_journey_pairs WHERE user_name = '$user_name' AND journey_id = $journey_id");
    if (mysqli_num_rows($result) > 0) {
        // entry already exists
        $response["success"] = 0;
        $response["message"] = "User already in the group";
    } else {
        $result = mysqli_query($db, "INSERT INTO user_journey_pairs (user_name, journey_id) VALUES ('$user_name', $journey_id)");
        if ($result) {
            // successfully inserted into database
            // update members field in journeys
            $result = mysqli_query($db, "UPDATE journeys SET members = members + 1 WHERE journey_id = $journey_id");
            if ($result) {
                $response["success"] = 1;
                $response["message"] = "Member successfully added";
            } else {
                // failed to update row
                $response["success"] = 0;
                $response["message"] = "Oops! An error occurred.";
            }

        } else {
            // failed to insert row
            $response["success"] = 0;
            $response["message"] = "Oops! An error occurred.";
        }
    }
 
} else {
    // no user found
    $response["success"] = 0;
    $response["message"] = "User does not exist";
}

// echoing JSON response
echo json_encode($response);
mysqli_close($db);
?>
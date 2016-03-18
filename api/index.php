<?php

require_once('model/user.php');
require_once('model/car_control_state.php');
require_once('db_connection.php');

$connectionConfig = getMySQLConnection();
$conn = new mysqli($connectionConfig->host, $connectionConfig->username, 
	$connectionConfig->password, $connectionConfig->db_name);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

Model::setDefaultConnection($conn);

function getCarStates() {
	$desired = CarControlState::getDesiredState();
	$latest = CarControlState::getLatestState();
	
	return array(
		'desired' => $desired->getData(),
		'latest' => $latest->getData()
	);
}

function validateControlState($controlStateData) {
	if( !is_array($controlStateData)) 
		return 'must be array';
	
	$required_number_keys = array('steering_value', 'speed_value');
	foreach ($required_number_keys as $required_number_key) {
		if( !isset($controlStateData[$required_number_key]))
			return 'missing '.$required_number_key;
		if( !is_numeric($controlStateData[$required_number_key]) )
			return $required_number_key.' must be a number';
	}
	return '';
}

function saveDesiredControlState($controlStateData) {
	global $conn;
	
	$msg = validateControlState($controlStateData);
	if( $msg )
		throw new Exception($msg);
	
	$stmt = $conn->prepare('update car_control_state set steering_value=?, speed_value=? where is_desired=1');
	if ( !$stmt )
		throw new Exception('Unable to prepare statement.');
	
	$stmt->bind_param('dd', $controlStateData['steering_value'], $controlStateData['speed_value']);
	$result = $stmt->execute();
	 
	return array('success' => $result);
}

function saveLatestControlState($controlStateData) {
	$msg = validateControlState($controlStateData);
	
	return array();
}

// FIXME: process routes.
// map routes to functions.
$queryString = $_SERVER["QUERY_STRING"];
$queryString = substr($queryString, strlen('page='));
$routes = array(
	'api/carState' => getCarStates,
	'api/saveDesiredState' => saveDesiredControlState,
	'api/saveLatestControlState' => saveLatestControlState
);

if ( isset($routes[$queryString]) ) {
	$response = $routes[$queryString]($_REQUEST);
	header('Content-type: application/json');
	echo json_encode($response);
}
else {
	http_response_code(404);
	echo 'Unable to match route: "'. $queryString . '"';
}
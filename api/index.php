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

function saveControlState($controlStateData, $is_desired) {
	global $conn;
	
	$msg = validateControlState($controlStateData);
	if( $msg )
		throw new Exception($msg);
	
	$stmt = $conn->prepare('update car_control_state set steering_value=?, speed_value=?, latest_change=NOW() where is_desired=?');
	if ( !$stmt )
		throw new Exception('Unable to prepare statement in saveControlState. is_desired='
			.$is_desired.', steering_value='.$controlStateData['steering_value'].', speed_value='.$controlStateData['speed_value'].', message='.
			$conn->error);
	
	$stmt->bind_param('ddi', $controlStateData['steering_value'], 
		$controlStateData['speed_value'], $is_desired);
	$result = $stmt->execute();
	 
	return array('success' => $result);
}

function saveDesiredControlState($controlStateData) {
	return saveControlState($controlStateData, 1);
}

function saveLatestControlState($controlStateData) {
	return saveControlState($controlStateData, 0);
}

function setRecordingVideo($isRecording) {
	global $conn;
	
	$isRecording = $isRecording ? 1 : 0;
	
	$stmt = $conn->prepare('update preference set is_recording=?, latest_change=now()');
	if ( !$stmt )
		throw new Exception('Unable to prepare statement in setRecordingVideo.');
	
	$stmt->bind_param('i', $isRecording);
	$result = $stmt->execute();	
	return array('success' => true);
}

function startRecording($data) {
	return setRecordingVideo(true);
}

function stopRecording($data) {
	return setRecordingVideo(false);
}

function getPreferences($data) {
	global $conn;
	require_once('model/preference.php');

	$res = $conn->query('select * from preference');
	$rawData = $res->fetch_assoc();
	$newPreference = new Preference($rawData);
	$result = $newPreference->getData();
	$res->close();
	
	return $result;
}

function saveCameraFrame($frameData) {
	global $conn;
	
	// check that there is a 'frame' specified.
	if (!isset($_FILES['frame']))
		throw new Exception('frame must be specified as a file parameter.  $_FILES = ' . print_r($_FILES, true) . ', _POST = '. print_r($_POST, true));
	if (!isset($_FILES['frame']['size']))
		throw new Exception('frame size must be specified');
	if (!isset($_FILES["frame"]["tmp_name"]))
		throw new Exception('frame tmp_name must be specified');
	
	$filename = $_FILES["frame"]["name"];
	$extension = pathinfo($filename, PATHINFO_EXTENSION);
	$extension = strtolower($extension);
	if ($extension != 'jpg')
		throw new Exception('extension of uploaded frame must be jpg.  '
			.'Actual extension: '.$extension);

	// check that the file size indicates that the file may be jpeg.
	// 125 bytes is the size of a 1 by 1 pixel jpeg.  Anything smaller can't be a valid jpeg.
	// 50 000 - 200 0000 is roughly the expected range.
	// over 16777216(16MB) is extremely large. 
	//   - Too large to be expected and too large to fit in a MySQL mediumblob field.
	$fileSize = $_FILES['frame']['size'];
	if ($fileSize === 0)
		throw new Exception("frame file size = 0.  _FILES = " . print_r($_FILES, true));
	if ($fileSize < 125 || $fileSize > 16777216)
		throw new Exception('frame file size must be in 125..16777216 but is ' . $fileSize);

	$jpegData = file_get_contents($_FILES["frame"]["tmp_name"]);
	if ( !$jpegData )
		throw new Exception('Unable to read jpeg data from temp file in saveCameraFrame.');
	
	$stmt = $conn->prepare('update frame set jpeg_data=?, latest_change=now() where id=1');
	if ( !$stmt )
		throw new Exception('Unable to prepare statement in saveCameraFrame.');
	
	$null = NULL;
	$stmt->bind_param("b", $null);
	$stmt->send_long_data(0, $jpegData);
	$result = $stmt->execute();	
	
	$result = array('success' => true);
	return $result;
}

function getCameraFrame() {
	global $conn;
	
	$stmt = $conn->prepare("select jpeg_data from frame where id=1");
	
	$stmt->execute();
	$stmt->store_result();

	$stmt->bind_result($jpegData);
	$stmt->fetch();
	
	header('Content-Type: image/jpeg');
	
	echo $jpegData;
	//$stmt->close();
	
	exit;
}

$queryString = $_SERVER["QUERY_STRING"];
$queryString = substr($queryString, strlen('page='));
$routes = array(
	'api/carStates' => 'getCarStates',
	'api/saveCameraFrame' => 'saveCameraFrame',
	'api/getCameraFrame' => 'getCameraFrame',
	'api/preferences' => 'getPreferences',
	'api/saveDesiredState' => 'saveDesiredControlState',
	'api/saveLatestControlState' => 'saveLatestControlState',
	'api/startRecording' => 'startRecording',
	'api/stopRecording' => 'stopRecording'
);

if (strpos($queryString, 'api/getCameraFrame') === 0) {
	$queryString = 'api/getCameraFrame';
}

function logMessage($msg) {
	$fp = fopen('log.txt', 'a+');
	if ($fp) {
		fprintf($fp, '%s - Message: %s'."\r\n", date("Y-m-d H:i:s"), $msg);
		fclose($fp);
	}
	else
		return '  Also, unable to append to log file';
}

if ( isset($routes[$queryString]) ) {
	logMessage('Processing request for route: ' . $queryString);
	try {
		$response = call_user_func($routes[$queryString], $_REQUEST);
	}
	catch (Exception $e) {
		$response = array('msg' => 'ERROR: '.$e->getMessage());
		http_response_code(500);
		$extraMessage = logMessage($e->getMessage());
		if ($extraMessage)
			$response['msg'] .= $extraMessage;
	}
	header('Content-Type: application/json');
	echo json_encode($response);
}
else {
	http_response_code(404);
	logMessage('404 - ' . $queryString);
	echo 'Unable to match route: "'. $queryString . '"';
}
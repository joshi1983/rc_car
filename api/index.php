<?php

require_once('model/user.php');
require_once('model/car_control_state.php');
require_once('db_connection.php');

$connectionConfig = getMySQLConnection();
$conn = new mysqli($connectionConfig->host, $connectionConfig->username, 
	$connectionConfig->password, $connectionConfig->db_name);

define('EVENT_TYPE_RECORDING_STATE_CHANGE', 2);
define('EVENT_TYPE_DESIRED_CAR_STATE_CHANGE', 3);
define('EVENT_TYPE_LATEST_CAR_STATE_CHANGE', 4);
define('EVENT_TYPE_FRAME_CHANGE', 5);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

Model::setDefaultConnection($conn);

function clearUselessEvents($eventType = 0) {
	global $conn;
	if ($eventType === 0) {	
		$stmt = $conn->execute(
			'delete from event as outer_event where id not in '
			.'(select MAX(id) from event where event.event_type=outer_event.event_type)'
		);
	}
	else {
		$res = $conn->query('select * from event where event_type='.$eventType);
		$rawData = $res->fetch_assoc();

		$recent_event_id = $rawData['id'];
		$stmt = $conn->execute(
			'delete from event as outer_event where id not in '
			.'(select MAX(id) from event where event.event_type=outer_event.event_type)'
		);
	}
}

function eventTriggered($eventType) {
	global $conn;

	$stmt = $conn->prepare('insert into `event`(`event_type`) values(?)');
	if ( !$stmt )
		throw new Exception('Unable to prepare statement in eventTriggered.');

	$stmt->bind_param('i', $eventType);
	$stmt->execute();
}

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
	
	$stmt = $conn->prepare('update car_control_state set steering_value=?, speed_value=? where is_desired=?');
	if ( !$stmt )
		throw new Exception('Unable to prepare statement in saveControlState. is_desired='
			.$is_desired.', steering_value='.$controlStateData['steering_value'].', speed_value='.$controlStateData['speed_value'].', message='.
			$conn->error);
	
	$stmt->bind_param('ddi', $controlStateData['steering_value'], 
		$controlStateData['speed_value'], $is_desired);
	$result = $stmt->execute();
	if ($is_desired)
		$eventType = EVENT_TYPE_DESIRED_CAR_STATE_CHANGE;
	else
		$eventType = EVENT_TYPE_LATEST_CAR_STATE_CHANGE;
	
	eventTriggered($eventType);
	 
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
	
	$stmt = $conn->prepare('update preference set is_recording=?');
	if ( !$stmt )
		throw new Exception('Unable to prepare statement in setRecordingVideo.');
	
	$stmt->bind_param('i', $isRecording);
	$result = $stmt->execute();
	eventTriggered(EVENT_TYPE_RECORDING_STATE_CHANGE);
	
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
	
	$stmt = $conn->prepare('update frame set jpeg_data=? where id=1');
	if ( !$stmt )
		throw new Exception('Unable to prepare statement in saveCameraFrame.');
	
	$null = NULL;
	$stmt->bind_param("b", $null);
	$stmt->send_long_data(0, $jpegData);
	$result = $stmt->execute();	
	eventTriggered(EVENT_TYPE_FRAME_CHANGE);
	
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

function waitForEventsNewerThan($latest, $eventTypes) {
	global $conn;

	// skip if latest is NULL.
	if (is_numeric($latest)) {
		$latest = intval($latest);
		$eventTypesExpression = '(' . implode(',', $eventTypes) . ')';
		$stmt = $conn->prepare("select max(id) as id, event_type from `event` where id > ? and event_type in "
			.$eventTypesExpression.' group by event_type');
		$stmt->bind_param('i', $latest);
		
		// wait until next frame is available.
		while (true) {
			$stmt->execute();
			$rawData = $stmt->fetch_all();
			if ($rawData) {
				$stmt->close();
				return $rawData;
			}
			
			// sleep for a short time(50000 microseconds or 50ms) 
			// to let MySQL and other resources do other things.
			usleep(50000);
		}
	}
	return $latest;
}

function getNextCameraFrame($latest) {
	//echo 'getNextCameraFrame function called.  latest = '.$latest;
	waitForEventsNewerThan($latest, array(EVENT_TYPE_FRAME_CHANGE));
	getCameraFrame();
}

// Used for Android application
function getNextTabletEvents($latest) {
	waitForEventsNewerThan(
		$latest, array(
			EVENT_TYPE_DESIRED_CAR_STATE_CHANGE, 
			EVENT_TYPE_LATEST_CAR_STATE_CHANGE
		)
	);
	
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

$queryString = $_SERVER["QUERY_STRING"]; 
// queryString could be something like 'rc_car/index.php?page=api/carStates'.
$queryString = substr($queryString, strlen('page='));
$route_regexs = array(
	'#api/getNextCameraFrame/([0-9]+)#i' => 'getNextCameraFrame',
	'#api/getNextTabletEvents/([0-9]+)#i' => 'getNextTabletEvents',
);

$routes = array(
	'api/carStates' => 'getCarStates',
	'api/saveCameraFrame' => 'saveCameraFrame',
	'api/getCameraFrame' => 'getCameraFrame',
	'api/getNextCameraFrame' => 'getNextCameraFrame',
	'api/preferences' => 'getPreferences',
	'api/saveDesiredState' => 'saveDesiredControlState',
	'api/saveLatestControlState' => 'saveLatestControlState',
	'api/startRecording' => 'startRecording',
	'api/stopRecording' => 'stopRecording'
);
$matches = array();
foreach($route_regexs as $regex => $func_name) {
	$result = preg_match($regex, $queryString, $matches);
	if( $result === FALSE )
		logMessage('error processing regex: '.$regex);
	else if ($result === 1) {
		try {
			header('Content-Type: text/plain');
			array_shift($matches); // remove the first match.
			$response = call_user_func_array($func_name, $matches);
		}
		catch (Exception $e) {
			$response = array('msg' => 'ERROR: '.$e->getMessage());
			http_response_code(500);
			$extraMessage = logMessage($e->getMessage());
			if ($extraMessage)
				$response['msg'] .= $extraMessage;
		}
	}
}

if (strpos($queryString, 'api/getCameraFrame') === 0) {
	$queryString = 'api/getCameraFrame';
}

if ( isset($routes[$queryString]) ) {
	// logMessage('Processing request for route: ' . $queryString);
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